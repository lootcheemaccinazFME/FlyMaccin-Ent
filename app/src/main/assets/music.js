const $=id=>document.getElementById(id);
const STORE='demonic_song_ai_v1';
const DAW_INBOX='demonic_daw_inbox_v1';
const DEFAULT_STEMS=['BEAT','SP3 MAIN LEAD','SP3 DOUBLE','SP3 LOW UNDERLAYER','AD-LIBS','HOOK RESPONSE','ECHO THROW','FX'];
let state=load();

function load(){
  try{return JSON.parse(localStorage.getItem(STORE)||'{}')}catch{return {}}
}
function persist(){
  state={...state,...readForm(),savedAt:new Date().toISOString()};
  localStorage.setItem(STORE,JSON.stringify(state));
  $('saveState').textContent='Saved';
}
function dirty(){$('saveState').textContent='Unsaved'}
function callBridge(method,...args){
  return new Promise((resolve,reject)=>{
    if(!window.AndroidBridge||typeof AndroidBridge[method]!=='function')return reject(new Error('Android bridge method unavailable: '+method));
    const id='m'+Date.now()+Math.random();
    window.__fmePending.set(id,{resolve,reject});
    AndroidBridge[method](id,...args);
  });
}
function ai(prompt){
  return new Promise((resolve,reject)=>{
    if(!window.AndroidBridge)return reject(new Error('Android bridge required.'));
    const id='m'+Date.now()+Math.random();
    window.__fmePending.set(id,{resolve,reject});
    AndroidBridge.generate(id,prompt,'gpt-5.6-terra');
  });
}
function readForm(){
  return {
    title:$('songTitle').value,
    brief:$('songBrief').value,
    beatCode:$('beatCode').value,
    deliveryMode:$('deliveryMode').value,
    register:Number($('register').value||0),
    energy:Number($('energy').value||0),
    projection:Number($('projection').value||0),
    chest:Number($('chest').value||0),
    brightness:Number($('brightness').value||0),
    adlibs:Number($('adlibs').value||0),
    fxCode:$('fxCode').value,
    masterLyrics:$('masterLyrics').value,
    performanceMap:$('performanceMap').value,
    voiceProfile:$('voiceProfile').value,
    renderPackage:$('renderPackage').value
  };
}
function hydrate(){
  const map={songTitle:'title',songBrief:'brief',beatCode:'beatCode',deliveryMode:'deliveryMode',register:'register',energy:'energy',projection:'projection',chest:'chest',brightness:'brightness',adlibs:'adlibs',fxCode:'fxCode',masterLyrics:'masterLyrics',performanceMap:'performanceMap',voiceProfile:'voiceProfile',renderPackage:'renderPackage'};
  for(const [id,key] of Object.entries(map))if(state[key]!==undefined)$(id).value=state[key];
  renderStems();
  checkVoice();
}
function songPrompt(){
  const f=readForm();
  return `You are the Demonic Song AI inside FME Studio Hut. Write one original full rap song from the creator brief. Preserve the creator's language, slang, profanity, explicitness, humor and Bay Area identity unless the creator specifically asks for edits. Do not sanitize or replace words just because they are explicit. Do not imitate any living rapper's exact voice or identifiable performance. SP3 remains the creator's own voice, sharpened with broad Bay Area DNA.\n\nTITLE: ${f.title}\nBRIEF: ${f.brief}\nBEAT CODE: ${f.beatCode}\nDELIVERY: ${f.deliveryMode}\nREGISTER: raised-mid ${f.register}\nENERGY: ${f.energy}%\nPROJECTION: ${f.projection}%\nCHEST WEIGHT: ${f.chest}\nBRIGHTNESS: ${f.brightness}\nAD-LIB DENSITY: ${f.adlibs}%\nFX: ${f.fxCode}\n\nMANDATORY SECTION ORDER:\n[Intro] -> [Verse 1] long -> [Hook] -> [Pre-Chorus] -> [Chorus] -> [Verse 2] long -> [Pre-Chorus] -> [Chorus] -> [Skit] -> [Verse 3] long -> [Spoken Bridge] -> [Outro] -> [Outro Echo].\n\nPERFORMANCE WRITING RULES:\n- loose, playful, grimy, commanding Bay talk/rap characteristics\n- natural Bay slang and conversational phrasing\n- integrated ad-libs throughout verses, not decoration only\n- use tight doubles on punchlines, low underlayers on key words, call-and-response hook doubles, pauses before punch bars, and echo tails\n- no singing unless explicitly requested\n- no direct cloning of any living rapper's exact voice or identifiable performance\n- return ONLY the finished lyrics with section labels; no explanation.`;
}
function performancePrompt(lyrics){
  const f=readForm();
  return `Build a machine-readable SP3 performance map for these master lyrics. Do NOT rewrite the lyrics. Return ONLY valid JSON.\n\nGlobal settings: beat=${f.beatCode}; delivery=${f.deliveryMode}; register=${f.register}; energy=${f.energy}; projection=${f.projection}; chestWeight=${f.chest}; brightness=${f.brightness}; adlibDensity=${f.adlibs}; fx=${f.fxCode}.\n\nJSON schema:\n{"version":1,"voice":"${f.voiceProfile||'SP3'}","global":{"beatCode":"...","delivery":"...","register":0,"energy":0,"projection":0,"chestWeight":0,"brightness":0,"fx":"..."},"sections":[{"name":"Verse 1","delivery":"...","bars":[{"index":1,"text":"exact lyric line","pocket":"on|ahead|behind|elastic","energy":0,"stress":["word"],"pauseBeforeMs":0,"pauseAfterMs":0,"adlibs":[{"text":"YEE!","position":"after"}],"doubleWords":["word"],"underlayerWords":["word"],"echoTail":"word"}]}],"stems":["BEAT","SP3 MAIN LEAD","SP3 DOUBLE","SP3 LOW UNDERLAYER","AD-LIBS","HOOK RESPONSE","ECHO THROW","FX"]}.\n\nMASTER LYRICS:\n${lyrics}`;
}
function buildPackageObject(){
  const f=readForm();
  let map=null;
  try{map=JSON.parse(f.performanceMap)}catch{}
  return {
    schema:'fme.demonic.song.render.v1',
    createdAt:new Date().toISOString(),
    project:{title:f.title,brief:f.brief},
    master:{lyrics:f.masterLyrics,providerMutable:false},
    beat:{code:f.beatCode},
    voice:{profileId:f.voiceProfile||'SP3',authorizedByCreator:true,delivery:f.deliveryMode,register:f.register,energy:f.energy,projection:f.projection,chestWeight:f.chest,brightness:f.brightness},
    fx:{code:f.fxCode,adlibDensity:f.adlibs},
    performanceMap:map||f.performanceMap,
    stems:DEFAULT_STEMS.map((name,index)=>({index:index+1,name,status:'planned'})),
    regeneration:{granularity:['word','bar','section','full'],preserveMasterByDefault:true}
  };
}
function renderStems(){
  $('stemPlan').innerHTML=DEFAULT_STEMS.map((s,i)=>`<div class="stemCard"><b>${String(i+1).padStart(2,'0')} ${s}</b><small>${i===0?'instrumental source':i===1?'authorized SP3 lead':'generated layer'}</small></div>`).join('');
}
async function generateLyrics(){
  try{
    $('musicStatus').textContent='Generating master lyrics…';
    const text=await ai(songPrompt());
    $('masterLyrics').value=text.trim();
    dirty();persist();
    $('musicStatus').textContent='Master lyrics generated and saved. Provider exports cannot overwrite this text.';
  }catch(e){$('musicStatus').textContent=e.message}
}
async function buildPerformance(){
  const lyrics=$('masterLyrics').value.trim();
  if(!lyrics)return $('musicStatus').textContent='Master lyrics are required first.';
  try{
    $('musicStatus').textContent='Directing SP3 performance…';
    let raw=await ai(performancePrompt(lyrics));
    raw=raw.replace(/^```json\s*|```$/g,'').trim();
    const parsed=JSON.parse(raw);
    $('performanceMap').value=JSON.stringify(parsed,null,2);
    dirty();persist();
    $('musicStatus').textContent='SP3 performance map built without changing the master lyrics.';
  }catch(e){$('musicStatus').textContent='Performance map failed: '+e.message}
}
async function regenerateSelection(){
  const ta=$('masterLyrics');
  if(ta.selectionStart===ta.selectionEnd)return $('musicStatus').textContent='Select one or more bars first.';
  const selected=ta.value.slice(ta.selectionStart,ta.selectionEnd);
  try{
    $('musicStatus').textContent='Regenerating selected bars…';
    const prompt=`Rewrite ONLY the selected rap bars below while preserving their meaning, explicitness, slang level, point of view, approximate syllable count, rhyme pressure and SP3 Bay talk/rap identity. Do not sanitize profanity or replace the creator's vocabulary merely because it is explicit. Do not imitate a living rapper's exact voice or identifiable performance. Return only replacement bars.\n\n${selected}`;
    const replacement=(await ai(prompt)).trim();
    ta.value=ta.value.slice(0,ta.selectionStart)+replacement+ta.value.slice(ta.selectionEnd);
    dirty();persist();
    $('musicStatus').textContent='Selected bars regenerated. Master updated only because the creator explicitly requested it.';
  }catch(e){$('musicStatus').textContent=e.message}
}
function buildPackage(){
  const pkg=buildPackageObject();
  $('renderPackage').value=JSON.stringify(pkg,null,2);
  dirty();persist();
  $('musicStatus').textContent='Render package built.';
}
async function saveVoiceConfig(){
  const endpoint=$('voiceEndpoint').value.trim(),token=$('voiceToken').value;
  if(!endpoint)return $('renderStatus').textContent='Voice endpoint is required.';
  try{
    await callBridge('saveVoiceConfig',endpoint,token||'');
    $('voiceToken').value='';
    $('renderStatus').textContent='Authorized voice endpoint saved securely on this device.';
    checkVoice();
  }catch(e){$('renderStatus').textContent=e.message}
}
async function clearVoiceConfig(){
  try{await callBridge('clearVoiceConfig');$('renderStatus').textContent='Voice endpoint configuration cleared.';checkVoice()}catch(e){$('renderStatus').textContent=e.message}
}
async function checkVoice(){
  try{
    const ready=window.AndroidBridge&&typeof AndroidBridge.hasVoiceConfig==='function'&&AndroidBridge.hasVoiceConfig();
    $('voiceStatus').textContent=ready?'VOICE READY':'VOICE NOT CONFIGURED';
    $('voiceStatus').className='badge '+(ready?'ok':'');
  }catch{$('voiceStatus').textContent='VOICE STATUS UNKNOWN'}
}
async function renderVoice(){
  const lyrics=$('masterLyrics').value.trim();
  if(!lyrics)return $('renderStatus').textContent='Master lyrics are required.';
  if(!$('performanceMap').value.trim())return $('renderStatus').textContent='Build the SP3 performance map first.';
  buildPackage();
  try{
    $('renderStatus').textContent='Rendering authorized SP3 voice…';
    const response=await callBridge('renderAuthorizedVoice',$('renderPackage').value);
    let data;try{data=JSON.parse(response)}catch{throw new Error('Voice endpoint returned invalid JSON.')}
    const audioBase64=data.audioBase64||data.audio_base64;
    const mime=data.mime||data.audioMime||'audio/wav';
    if(audioBase64){
      $('voicePreview').src=`data:${mime};base64,${audioBase64}`;
      $('voicePreview').style.display='block';
    }
    state.lastVoiceRender={at:new Date().toISOString(),response:data};persist();
    $('renderStatus').textContent=audioBase64?'Voice render complete. Preview loaded.':'Voice job accepted: '+(data.jobId||data.status||'submitted');
  }catch(e){$('renderStatus').textContent='Voice render failed: '+e.message}
}
function sendToDaw(){
  const pkg=buildPackageObject();
  pkg.daw={target:'Demonic DAW',trackLayout:DEFAULT_STEMS,status:'READY'};
  localStorage.setItem(DAW_INBOX,JSON.stringify(pkg));
  $('renderPackage').value=JSON.stringify(pkg,null,2);
  persist();
  $('musicStatus').textContent='Render package placed in the Demonic DAW inbox.';
}
function exportPackage(){
  const text=$('renderPackage').value.trim()||JSON.stringify(buildPackageObject(),null,2);
  const blob=new Blob([text],{type:'application/json'}),url=URL.createObjectURL(blob),a=document.createElement('a');
  a.href=url;a.download=(($('songTitle').value||'demonic-song').replace(/[^a-z0-9-_]+/gi,'_'))+'_render.json';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);
}

['songTitle','songBrief','beatCode','deliveryMode','register','energy','projection','chest','brightness','adlibs','fxCode','masterLyrics','performanceMap','voiceProfile','renderPackage'].forEach(id=>$(id).addEventListener('input',dirty));
$('generateLyrics').onclick=generateLyrics;
$('buildPerformance').onclick=buildPerformance;
$('buildPackage').onclick=buildPackage;
$('saveMaster').onclick=persist;
$('copyLyrics').onclick=()=>navigator.clipboard.writeText($('masterLyrics').value);
$('regenerateSelection').onclick=regenerateSelection;
$('saveVoiceConfig').onclick=saveVoiceConfig;
$('clearVoiceConfig').onclick=clearVoiceConfig;
$('renderVoice').onclick=renderVoice;
$('sendToDaw').onclick=sendToDaw;
$('exportPackage').onclick=exportPackage;
hydrate();