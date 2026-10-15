import { retryDecision, stableAttemptKey } from './resilience-policy';
import { canUseSession, decideSync, advanceCursor } from './device-sync-policy';
import { evaluateProductionRelease, ProductionEvidence } from './release-gates';

export const FME_GATEWAY_VERSION = '0.17.0-dev';
export const COMMAND_1821 = '1821';
export const VOICE_1821 = 'FME_HAHA_RS2_HIGH_ENERGY_V1';

export type ProviderState = 'READY'|'NOT_CONFIGURED'|'NOT_CONNECTED'|'BLOCKED'|'DEGRADED';
export type JobState = 'QUEUED'|'RUNNING'|'WAITING_PROVIDER'|'RETRY_WAIT'|'COMPLETED'|'COMPLETED_WITH_ERRORS'|'FAILED'|'CANCELLED';
export type ProviderResult = { providerId:string; requestId:string; ok:boolean; output?:unknown; error?:string; provenance:{provider:string; receivedAt:string; externalId?:string} };
export type GenerationSlot = { pair:number; cut:'A'|'B'; bpm:114|94; voiceCode:string; status:'PENDING_PROVIDER'|'COMPLETE'|'FAILED'; result?:ProviderResult };
export type Job = { id:string; command:string; state:JobState; idempotencyKey:string; slots:GenerationSlot[]; attempts:number; cancelled:boolean; createdAt:string; updatedAt:string };

export interface Store {
  findJobByIdempotency(key:string):Promise<Job|undefined>;
  saveJob(job:Job):Promise<void>;
  audit(event:string, details:Record<string,unknown>):Promise<void>;
  providerState(providerId:string):Promise<ProviderState>;
  saveProviderResult(jobId:string, slot:number, result:ProviderResult):Promise<void>;
}

export interface ProviderRuntime {
  execute(input:{jobId:string; slot:number; bpm:number; voiceCode:string; idempotencyKey:string}):Promise<ProviderResult>;
}

const now=()=>new Date().toISOString();
const uid=(prefix:string)=>`${prefix}-${Date.now()}-${Math.random().toString(36).slice(2,10)}`;

export function expand1821():GenerationSlot[]{
  const slots:GenerationSlot[]=[];
  for(let pair=1;pair<=25;pair++){
    slots.push({pair,cut:'A',bpm:114,voiceCode:VOICE_1821,status:'PENDING_PROVIDER'});
    slots.push({pair,cut:'B',bpm:94,voiceCode:VOICE_1821,status:'PENDING_PROVIDER'});
  }
  if(slots.length!==50) throw new Error('1821 invariant violated: expected exactly 50 slots');
  return slots;
}

export class FmeGatewayCore {
  constructor(private store:Store, private providers:Record<string,ProviderRuntime>){ }

  health(){ return {ok:true,version:FME_GATEWAY_VERSION,time:now()}; }

  async executeCommand(command:string,idempotencyKey:string):Promise<Job>{
    if(!idempotencyKey) throw new Error('idempotencyKey required');
    const existing=await this.store.findJobByIdempotency(idempotencyKey);
    if(existing) return existing;
    if(command!==COMMAND_1821) throw new Error(`Unsupported command: ${command}`);
    const t=now();
    const job:Job={id:uid('job'),command,state:'WAITING_PROVIDER',idempotencyKey,slots:expand1821(),attempts:0,cancelled:false,createdAt:t,updatedAt:t};
    await this.store.saveJob(job);
    await this.store.audit('COMMAND_ACCEPTED',{jobId:job.id,command,slotCount:50});
    return job;
  }

  async runProviderSlot(job:Job,slotIndex:number,providerId:string):Promise<Job>{
    if(job.cancelled) return {...job,state:'CANCELLED',updatedAt:now()};
    const provider=this.providers[providerId];
    const state=await this.store.providerState(providerId);
    if(!provider || state!=='READY'){
      await this.store.audit('PROVIDER_UNAVAILABLE',{jobId:job.id,providerId,state});
      return {...job,state:'WAITING_PROVIDER',updatedAt:now()};
    }
    const slot=job.slots[slotIndex];
    if(!slot) throw new Error('slot out of range');
    const attempt=job.attempts+1;
    const attemptKey=stableAttemptKey(job.id,providerId,attempt);
    try{
      const result=await provider.execute({jobId:job.id,slot:slotIndex,bpm:slot.bpm,voiceCode:slot.voiceCode,idempotencyKey:attemptKey});
      await this.store.saveProviderResult(job.id,slotIndex,result);
      const slots=[...job.slots]; slots[slotIndex]={...slot,status:result.ok?'COMPLETE':'FAILED',result};
      const pending=slots.some(s=>s.status==='PENDING_PROVIDER');
      const failed=slots.some(s=>s.status==='FAILED');
      const nextState:JobState=pending?'RUNNING':failed?'COMPLETED_WITH_ERRORS':'COMPLETED';
      const updated={...job,slots,attempts:attempt,state:nextState,updatedAt:now()};
      await this.store.saveJob(updated);
      return updated;
    }catch(error){
      const decision=retryDecision(attempt,true);
      const updated={...job,attempts:attempt,state:(decision.retry?'RETRY_WAIT':'FAILED') as JobState,updatedAt:now()};
      await this.store.audit(decision.deadLetter?'JOB_DEAD_LETTERED':'JOB_RETRY_SCHEDULED',{jobId:job.id,providerId,attempt,delayMs:decision.delayMs,error:String(error)});
      await this.store.saveJob(updated);
      return updated;
    }
  }

  verifyDeviceSession(status:'ACTIVE'|'REVOKED',revokedAt?:string|null){ return canUseSession(status,revokedAt); }
  syncDecision(receipted:boolean,localVersion:number,incomingVersion:number){ return decideSync(receipted,localVersion,incomingVersion); }
  nextCursor(current:number,delivered:number){ return advanceCursor(current,delivered); }
  releaseDecision(evidence:ProductionEvidence){ return evaluateProductionRelease(evidence); }
}
