const HOUSE_STATE_KEY='fme_house_workspaces_v1';

const HOUSE_DEFINITIONS={
  musicMedia:{
    name:'Music & Media House',
    purpose:'Audio creation + entertainment',
    loop:[
      {id:'enter',label:'Enter House',required:true},
      {id:'beat',label:'Make / Select Beat',required:true},
      {id:'lyrics',label:'Write Lyrics',required:true},
      {id:'vocals',label:'Record Vocals',required:true},
      {id:'mix',label:'Mix / Play Song',required:true},
      {id:'save',label:'Save Project',required:true},
      {id:'tv',label:'Kick Back at TV',required:false}
    ],
    rooms:[
      {id:'beatRoom',name:'Beat Room',description:'Create, choose, or document the beat foundation.'},
      {id:'lyricRoom',name:'Lyric Room',description:'Write and refine lyrics around the selected beat.'},
      {id:'vocalBooth',name:'Vocal Booth',description:'Track vocal takes and performance notes.'},
      {id:'mixRoom',name:'Mix Room',description:'Mix, audition, and finalize the song playback state.'},
      {id:'vault',name:'Project Vault',description:'Save the House project and preserve its current state.'},
      {id:'tvRoom',name:'TV Lounge',description:'Optional entertainment / playback destination after the work loop.'}
    ]
  }
};

const FUTURE_HOUSE_PATTERN=['Comic Studio','Bookwriter','Video Studio','Visual Studio'];
const byId=id=>document.getElementById(id);
let houseState=loadHouseState();
let activeHouse='musicMedia';

function blankHouseState(key){
  const def=HOUSE_DEFINITIONS[key];
  return {
    projectName:'Untitled House Project',
    currentStep:'enter',
    completed:{},
    notes:Object.fromEntries(def.loop.map(step=>[step.id,''])),
    updatedAt:new Date().toISOString()
  };
}

function loadHouseState(){
  try{return JSON.parse(localStorage.getItem(HOUSE_STATE_KEY)||'{}')}catch(_){return {}}
}

function state(){
  if(!houseState[activeHouse])houseState[activeHouse]=blankHouseState(activeHouse);
  return houseState[activeHouse];
}

function persistHouse(){
  state().updatedAt=new Date().toISOString();
  localStorage.setItem(HOUSE_STATE_KEY,JSON.stringify(houseState));
  const s=byId('houseSaveState');
  if(s)s.textContent='House saved';
}

function stepIndex(id){return HOUSE_DEFINITIONS[activeHouse].loop.findIndex(step=>step.id===id)}

function canOpenStep(id){
  const def=HOUSE_DEFINITIONS[activeHouse],s=state(),idx=stepIndex(id);
  if(idx<=0)return true;
  for(let i=0;i<idx;i++){
    if(def.loop[i].required&&!s.completed[def.loop[i].id])return false;
  }
  return true;
}

function nextRequiredStep(){
  const def=HOUSE_DEFINITIONS[activeHouse],s=state();
  return def.loop.find(step=>step.required&&!s.completed[step.id])?.id||'tv';
}

function renderHouse(){
  const def=HOUSE_DEFINITIONS[activeHouse],s=state();
  byId('houseName').textContent=def.name;
  byId('housePurpose').textContent=def.purpose;
  byId('houseProjectName').value=s.projectName;
  byId('houseLoop').innerHTML=def.loop.map((step,i)=>{
    const complete=!!s.completed[step.id],current=s.currentStep===step.id,locked=!canOpenStep(step.id);
    return `<button class="houseStep ${complete?'complete':''} ${current?'current':''}" data-house-step="${step.id}" ${locked?'disabled':''}><span>${i+1}</span><b>${step.label}</b><small>${step.required?'CORE':'OPTIONAL'}</small></button>`;
  }).join('');
  const selected=def.loop.find(step=>step.id===s.currentStep)||def.loop[0];
  const room=def.rooms[Math.min(stepIndex(selected.id)-1,def.rooms.length-1)]||def.rooms[0];
  byId('houseStageTitle').textContent=selected.label;
  byId('houseRoomName').textContent=selected.id==='enter'?'Front Door / House Hub':room.name;
  byId('houseRoomDescription').textContent=selected.id==='enter'?'Start the reusable House workflow. Finish each required room in order, then the TV Lounge remains optional.':room.description;
  byId('houseStepNotes').value=s.notes[selected.id]||'';
  byId('houseCompleteStep').textContent=s.completed[selected.id]?'Reopen Step':(selected.required?'Complete Step':'Mark Optional Step');
  byId('houseCompleteStep').dataset.step=selected.id;
  byId('houseProgress').textContent=`${def.loop.filter(x=>x.required&&s.completed[x.id]).length}/${def.loop.filter(x=>x.required).length} core steps complete`;
  byId('futureHouses').textContent='Reusable pattern ready for: '+FUTURE_HOUSE_PATTERN.join(' · ');
  document.querySelectorAll('[data-house-step]').forEach(btn=>btn.onclick=()=>{
    if(!canOpenStep(btn.dataset.houseStep))return;
    s.currentStep=btn.dataset.houseStep;
    persistHouse();
    renderHouse();
  });
}

byId('houseProjectName').oninput=()=>{state().projectName=byId('houseProjectName').value;persistHouse()};
byId('houseStepNotes').oninput=()=>{state().notes[state().currentStep]=byId('houseStepNotes').value;persistHouse()};
byId('houseCompleteStep').onclick=()=>{
  const s=state(),id=byId('houseCompleteStep').dataset.step;
  s.completed[id]=!s.completed[id];
  s.currentStep=s.completed[id]?nextRequiredStep():id;
  persistHouse();
  renderHouse();
};
byId('houseReset').onclick=()=>{
  houseState[activeHouse]=blankHouseState(activeHouse);
  persistHouse();
  renderHouse();
};
byId('houseSave').onclick=()=>persistHouse();

renderHouse();
