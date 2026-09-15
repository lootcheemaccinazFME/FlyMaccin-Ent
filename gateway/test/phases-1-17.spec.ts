import { expand1821, FmeGatewayCore, Store, Job, ProviderState } from '../src/fme-gateway-core';
import { MCP_TOOLS, dispatchMcp } from '../src/mcp-contract';
import { retryDecision } from '../src/resilience-policy';
import { decideSync } from '../src/device-sync-policy';
import { evaluateProductionRelease } from '../src/release-gates';

function assert(v:unknown,msg:string){if(!v)throw new Error(msg);}

class MemoryStore implements Store{
  jobs=new Map<string,Job>(); states=new Map<string,ProviderState>(); audits:any[]=[];
  async findJobByIdempotency(k:string){return this.jobs.get(k)}
  async saveJob(j:Job){this.jobs.set(j.idempotencyKey,j)}
  async audit(event:string,details:Record<string,unknown>){this.audits.push({event,details})}
  async providerState(id:string){return this.states.get(id)||'NOT_CONFIGURED'}
  async saveProviderResult(){return}
}

async function main(){
  const slots=expand1821();
  assert(slots.length===50,'1821 must create 50 slots');
  assert(slots.filter(s=>s.cut==='A'&&s.bpm===114).length===25,'25 A/114 slots required');
  assert(slots.filter(s=>s.cut==='B'&&s.bpm===94).length===25,'25 B/94 slots required');
  assert(slots.every(s=>s.status==='PENDING_PROVIDER'),'provider success must never be fabricated');

  const store=new MemoryStore();
  const core=new FmeGatewayCore(store,{});
  const first=await core.executeCommand('1821','idem-1');
  const second=await core.executeCommand('1821','idem-1');
  assert(first.id===second.id,'idempotency must reuse job');
  assert(first.state==='WAITING_PROVIDER','unconnected provider state must remain waiting');
  assert(MCP_TOOLS.length===10,'v0.5 MCP contract must expose 10 tools');
  const mcp=await dispatchMcp(core,{jsonrpc:'2.0',id:1,method:'tools/call',params:{name:'execute_1821',arguments:{idempotencyKey:'mcp-1'}}});
  assert((mcp as any).result.providerSlots===50,'MCP 1821 must report 50 slots');

  assert(retryDecision(1,true).retry===true,'retryable first attempt should retry');
  assert(retryDecision(5,true).deadLetter===true,'fifth failed attempt should dead-letter');
  assert(decideSync(true,1,2)==='DUPLICATE','receipt must suppress duplicate');
  assert(decideSync(false,2,1)==='STALE','older sync version must be stale');
  assert(core.verifyDeviceSession('REVOKED',new Date().toISOString())===false,'revoked device cannot operate');

  const blocked=evaluateProductionRelease({publicMcp:false,connectorAuth:false,providerCommand:false,command1821:false,androidResultSync:false,physicalAndroidE2E:false,backupVerified:true,auditEnabled:true,privacyReviewed:false,apkCompatible:false});
  assert(blocked.status==='BLOCKED','release must fail closed');
  assert(blocked.missing.includes('physical_android_e2e'),'physical Android evidence is mandatory');
  console.log('PHASES_1_17_ACCEPTANCE: PASS');
}
main().catch(e=>{console.error(e);process.exit(1)});
