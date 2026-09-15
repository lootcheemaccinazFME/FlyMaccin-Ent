import { FmeGatewayCore } from './fme-gateway-core';

export const MCP_TOOLS = [
  {name:'get_fme_status',risk:'read'},
  {name:'list_projects',risk:'read'},
  {name:'get_job_status',risk:'read'},
  {name:'run_fme_command',risk:'write'},
  {name:'execute_1821',risk:'write'},
  {name:'list_assets',risk:'read'},
  {name:'get_asset',risk:'read'},
  {name:'save_generated_content',risk:'write'},
  {name:'approve_asset',risk:'write'},
  {name:'get_umd_rules',risk:'read'},
] as const;

export function protectedResourceMetadata(origin:string){
  const base=origin.replace(/\/$/,'');
  return {resource:`${base}/mcp`,authorization_servers:[base],scopes_supported:['fme.read','fme.write','offline_access']};
}

export function authorizationServerMetadata(origin:string){
  const base=origin.replace(/\/$/,'');
  return {
    issuer:base,
    authorization_endpoint:`${base}/oauth/authorize`,
    token_endpoint:`${base}/oauth/token`,
    registration_endpoint:`${base}/oauth/register`,
    code_challenge_methods_supported:['S256'],
    grant_types_supported:['authorization_code','refresh_token'],
    scopes_supported:['fme.read','fme.write','offline_access'],
  };
}

export async function dispatchMcp(core:FmeGatewayCore,request:{jsonrpc:string;id?:unknown;method:string;params?:any}){
  const id=request.id??null;
  if(request.jsonrpc!=='2.0') return {jsonrpc:'2.0',id,error:{code:-32600,message:'Invalid Request'}};
  if(request.method==='tools/list') return {jsonrpc:'2.0',id,result:{tools:MCP_TOOLS}};
  if(request.method==='tools/call'){
    const name=request.params?.name;
    const args=request.params?.arguments??{};
    if(name==='get_fme_status') return {jsonrpc:'2.0',id,result:core.health()};
    if(name==='execute_1821' || (name==='run_fme_command'&&args.command==='1821')){
      const job=await core.executeCommand('1821',String(args.idempotencyKey||''));
      return {jsonrpc:'2.0',id,result:{jobId:job.id,state:job.state,providerSlots:job.slots.length}};
    }
    return {jsonrpc:'2.0',id,error:{code:-32601,message:`Tool not implemented in core dispatcher: ${name}`}};
  }
  return {jsonrpc:'2.0',id,error:{code:-32601,message:'Method not found'}};
}
