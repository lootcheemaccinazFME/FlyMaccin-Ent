from pathlib import Path
import re
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'demonic-ai-studio-hut')
assets = root / 'app/src/main/assets'
index = assets / 'index.html'
gradle = root / 'app/build.gradle.kts'

if not index.exists():
    raise SystemExit('Missing main index.html')

studio_js = r'''(() => {
'use strict';
const KEY='demonic_studio_os_v213';
const DEFAULT={projects:[],current:null,models:[],voices:[],assets:[],snapshots:[],autoSave:true,lastSaved:null};
const load=()=>{try{return Object.assign({},DEFAULT,JSON.parse(localStorage.getItem(KEY)||'{}'))}catch(_){return {...DEFAULT}}};
let state=load();
const save=()=>{state.lastSaved=new Date().toISOString();localStorage.setItem(KEY,JSON.stringify(state));renderStatus()};
const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const id=()=>Date.now().toString(36)+Math.random().toString(36).slice(2,7);
const current=()=>state.projects.find(p=>p.id===state.current)||null;
const stages=[['creator','Creator AI'],['beatmaker','Beat Maker'],['daw','Demonic DAW'],['voice','Voice Studio'],['visual','Visual Studio'],['game','Game Maker'],['library','FME Library'],['release','Release Package']];
const roadmap=[
['Interactive multitask QA','Implemented test target'],['Release signing','Approved / requires release keystore'],['Local model manager','Registry implemented; inference engine pending'],['Persistent AI/project memory','Project persistence implemented'],['Semantic FME Library search','Approved / indexing engine pending'],['Agent handoffs','Pipeline routing implemented; autonomous agents pending'],['DAW effects/automation/MIDI/stems/mastering','Approved / audio engines pending'],['SF2/SFZ sampler','Approved / sampler engine pending'],['Beat Maker sequencer/piano roll/sampler/808/chop','Approved / deeper engine pending'],['Voice Studio recording/STT/TTS/profiles/cloning controls','Voice registry implemented; engines vary'],['Visual Studio generation/editing/segmentation','Approved / generation engines pending'],['Game Maker scene/physics/UI/debug/export','Approved / build engine pending'],['FME knowledge graph','Registry foundation implemented'],['Universal Send To pipeline','Implemented'],['Autosave/version snapshots/crash recovery','Autosave + snapshots implemented'],['Android widgets/share/file associations/media controls','Approved / native integration pending'],['Natural-language orchestration','Command templates implemented; autonomous orchestration pending']
];
function route(name){try{if(window.PP&&typeof PP.nav==='function'){PP.nav(name);close();return}}catch(_){} alert('Room routing is not exposed by this build. Open '+name+' from the main app navigation.');}
function addProject(template='Custom Project'){
 const name=prompt('Project name',template==='Single'?'New FME Single':template==='Game'?'Pinball Basketball':template==='Book'?'New Book Project':'New Project');
 if(!name)return;
 const p={id:id(),name,type:template,created:new Date().toISOString(),updated:new Date().toISOString(),stage:'creator',notes:'',links:[],status:'ACTIVE'};
 state.projects.unshift(p);state.current=p.id;snapshot('Project created');save();render();
}
function updateNotes(v){const p=current();if(!p)return;p.notes=v;p.updated=new Date().toISOString();if(state.autoSave)save();}
function setStage(s){const p=current();if(!p)return alert('Create or select a project first.');p.stage=s;p.updated=new Date().toISOString();save();render();if(['creator','beatmaker','daw','game','library'].includes(s))route(s);}
function snapshot(label='Manual snapshot'){const p=current();state.snapshots.unshift({id:id(),at:new Date().toISOString(),label,project:p?JSON.parse(JSON.stringify(p)):null});state.snapshots=state.snapshots.slice(0,30);save();}
function restoreSnapshot(sid){const s=state.snapshots.find(x=>x.id===sid);if(!s||!s.project)return;if(!confirm('Restore this project snapshot?'))return;const i=state.projects.findIndex(p=>p.id===s.project.id);if(i>=0)state.projects[i]=JSON.parse(JSON.stringify(s.project));else state.projects.unshift(JSON.parse(JSON.stringify(s.project)));state.current=s.project.id;save();render();}
function addRegistry(kind){const name=prompt(kind==='models'?'Model name':kind==='voices'?'Voice profile name':'Asset/character name');if(!name)return;state[kind].unshift({id:id(),name,status:'REGISTERED',created:new Date().toISOString()});save();render();}
function exportState(){const blob=new Blob([JSON.stringify(state,null,2)],{type:'application/json'});const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='Demonic_Studio_OS_v2.1.3_backup.json';a.click();setTimeout(()=>URL.revokeObjectURL(a.href),1000);}
function importState(file){const r=new FileReader();r.onload=()=>{try{const v=JSON.parse(r.result);state=Object.assign({},DEFAULT,v);save();render();alert('Studio OS backup imported.')}catch(e){alert('Invalid Studio OS backup.')}};r.readAsText(file);}
function renderStatus(){const e=document.getElementById('dso-status');if(e)e.textContent=`${state.projects.length} projects • ${state.autoSave?'autosave ON':'autosave OFF'}${state.lastSaved?' • saved '+new Date(state.lastSaved).toLocaleTimeString():''}`;}
function projectRows(){return state.projects.map(p=>`<button class="dso-project ${p.id===state.current?'active':''}" data-pick="${p.id}"><b>${esc(p.name)}</b><span>${esc(p.type)} • ${esc(p.status)} • ${esc(p.stage)}</span></button>`).join('')||'<div class="dso-empty">No projects yet.</div>'}
function registryRows(items){return items.map(x=>`<div class="dso-reg"><b>${esc(x.name)}</b><span>${esc(x.status)}</span></div>`).join('')||'<div class="dso-empty">Nothing registered yet.</div>'}
function roadmapRows(){return roadmap.map(([a,b])=>`<div class="dso-road"><b>${esc(a)}</b><span>${esc(b)}</span></div>`).join('')}
function diagnostics(){return [
 ['Storage',navigator.storage?'available':'limited'],['MediaDevices',navigator.mediaDevices?'available':'limited'],['WebAudio',window.AudioContext||window.webkitAudioContext?'available':'limited'],['WebAssembly',window.WebAssembly?'available':'limited'],['Workers',window.Worker?'available':'limited'],['Share API',navigator.share?'available':'limited'],['Online',navigator.onLine?'yes':'no']
].map(([a,b])=>`<div class="dso-reg"><b>${a}</b><span>${b}</span></div>`).join('')}
function render(){
 const body=document.getElementById('dso-body');if(!body)return;const p=current();
 body.innerHTML=`
 <section><h3>Project Command</h3><div class="dso-actions"><button data-new="Single">New Single</button><button data-new="Game">New Game</button><button data-new="Book">New Book</button><button data-new="Custom">New Project</button></div>${projectRows()}</section>
 <section><h3>Current Project</h3>${p?`<div class="dso-current"><b>${esc(p.name)}</b><span>${esc(p.type)} • ${esc(p.status)}</span></div><textarea id="dso-notes" placeholder="Project notes, decisions, handoffs...">${esc(p.notes)}</textarea><div class="dso-pipeline">${stages.map(([r,n])=>`<button data-stage="${r}" class="${p.stage===r?'active':''}">${n}</button>`).join('')}</div>`:'<div class="dso-empty">Select a project to activate the pipeline.</div>'}</section>
 <section><h3>Registries</h3><div class="dso-cols"><div><div class="dso-head"><b>Models</b><button data-add="models">+</button></div>${registryRows(state.models)}</div><div><div class="dso-head"><b>Voices</b><button data-add="voices">+</button></div>${registryRows(state.voices)}</div><div><div class="dso-head"><b>FME Assets</b><button data-add="assets">+</button></div>${registryRows(state.assets)}</div></div></section>
 <section><h3>Reliability</h3><div class="dso-actions"><button id="dso-snap">Snapshot</button><button id="dso-export">Export Backup</button><label class="dso-import">Import Backup<input id="dso-import" type="file" accept="application/json"></label><button id="dso-auto">Autosave: ${state.autoSave?'ON':'OFF'}</button></div>${state.snapshots.slice(0,8).map(s=>`<button class="dso-snapshot" data-restore="${s.id}"><b>${esc(s.label)}</b><span>${new Date(s.at).toLocaleString()}</span></button>`).join('')||'<div class="dso-empty">No snapshots yet.</div>'}</section>
 <section><h3>Device Capability Check</h3>${diagnostics()}</section>
 <section><h3>Approved Build Roadmap</h3><div class="dso-roadmap">${roadmapRows()}</div></section>`;
 renderStatus();
 body.querySelectorAll('[data-new]').forEach(b=>b.onclick=()=>addProject(b.dataset.new));
 body.querySelectorAll('[data-pick]').forEach(b=>b.onclick=()=>{state.current=b.dataset.pick;save();render()});
 body.querySelectorAll('[data-stage]').forEach(b=>b.onclick=()=>setStage(b.dataset.stage));
 body.querySelectorAll('[data-add]').forEach(b=>b.onclick=()=>addRegistry(b.dataset.add));
 body.querySelectorAll('[data-restore]').forEach(b=>b.onclick=()=>restoreSnapshot(b.dataset.restore));
 const n=document.getElementById('dso-notes');if(n)n.oninput=e=>updateNotes(e.target.value);
 document.getElementById('dso-snap').onclick=()=>{snapshot();render()};
 document.getElementById('dso-export').onclick=exportState;
 document.getElementById('dso-import').onchange=e=>{if(e.target.files[0])importState(e.target.files[0])};
 document.getElementById('dso-auto').onclick=()=>{state.autoSave=!state.autoSave;save();render()};
}
function open(){document.getElementById('dso-panel').classList.add('open');render()}
function close(){document.getElementById('dso-panel').classList.remove('open')}
function mount(){
 if(document.getElementById('dso-launch'))return;
 const launch=document.createElement('button');launch.id='dso-launch';launch.textContent='OS';launch.title='Demonic Studio OS';launch.onclick=open;document.body.appendChild(launch);
 const panel=document.createElement('div');panel.id='dso-panel';panel.innerHTML='<div class="dso-shell"><header><div><b>Demonic Studio OS</b><span id="dso-status"></span></div><button id="dso-close">×</button></header><main id="dso-body"></main></div>';document.body.appendChild(panel);
 document.getElementById('dso-close').onclick=close;panel.onclick=e=>{if(e.target===panel)close()};render();
 window.DemonicStudioOS={open,close,save,route,getState:()=>JSON.parse(JSON.stringify(state))};
}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',mount);else mount();
})();
'''

studio_css = r'''#dso-launch{position:fixed;right:12px;bottom:16px;z-index:2147483000;width:50px;height:50px;border-radius:50%;border:1px solid #9b7cff;background:#17131f;color:#fff;font-weight:800;box-shadow:0 8px 30px #0008}#dso-panel{display:none;position:fixed;inset:0;z-index:2147483001;background:#000b;padding:10px;box-sizing:border-box}#dso-panel.open{display:block}.dso-shell{max-width:980px;height:calc(100vh - 20px);margin:auto;background:#0c0d11;color:#f4f4f6;border:1px solid #34313d;border-radius:18px;overflow:hidden;display:flex;flex-direction:column}.dso-shell header{display:flex;align-items:center;justify-content:space-between;padding:14px 16px;border-bottom:1px solid #282631;background:#121119}.dso-shell header div{display:flex;flex-direction:column;gap:3px}.dso-shell header span{font-size:11px;color:#aaa5b4}.dso-shell header button{font-size:28px;background:transparent;border:0;color:#fff}.dso-shell main{overflow:auto;padding:14px}.dso-shell section{background:#12131a;border:1px solid #262832;border-radius:14px;padding:12px;margin-bottom:12px}.dso-shell h3{margin:0 0 10px;font-size:14px}.dso-actions,.dso-pipeline{display:flex;gap:8px;flex-wrap:wrap;margin-bottom:10px}.dso-shell button,.dso-import{background:#201c2b;color:#eee;border:1px solid #403753;border-radius:9px;padding:9px 11px;font:inherit}.dso-shell button.active,.dso-project.active{border-color:#b18cff;background:#302343}.dso-project,.dso-snapshot{display:flex!important;width:100%;justify-content:space-between;text-align:left;margin:5px 0}.dso-project span,.dso-snapshot span,.dso-current span,.dso-reg span,.dso-road span{font-size:11px;color:#aaa5b4}.dso-current{display:flex;justify-content:space-between;margin-bottom:8px}.dso-shell textarea{width:100%;min-height:95px;box-sizing:border-box;background:#090a0e;color:#fff;border:1px solid #30323d;border-radius:10px;padding:10px;margin-bottom:10px}.dso-cols{display:grid;grid-template-columns:repeat(3,1fr);gap:10px}.dso-head,.dso-reg,.dso-road{display:flex;align-items:center;justify-content:space-between;gap:10px;padding:7px 0;border-bottom:1px solid #242630}.dso-head button{padding:3px 9px}.dso-empty{font-size:12px;color:#8f8b98;padding:8px 0}.dso-import input{display:none}.dso-roadmap{display:grid;grid-template-columns:1fr 1fr;gap:0 14px}@media(max-width:680px){.dso-cols,.dso-roadmap{grid-template-columns:1fr}.dso-shell main{padding:10px}}'''

(assets / 'studio_os.js').write_text(studio_js, encoding='utf-8')
(assets / 'studio_os.css').write_text(studio_css, encoding='utf-8')

html = index.read_text(encoding='utf-8')
if 'studio_os.css' not in html:
    html = html.replace('</head>', '<link rel="stylesheet" href="studio_os.css">\n</head>')
if 'studio_os.js' not in html:
    html = html.replace('</body>', '<script src="studio_os.js"></script>\n</body>')
index.write_text(html, encoding='utf-8')

g = gradle.read_text(encoding='utf-8')
g = re.sub(r'versionName\s*=\s*"2\.1\.2"', 'versionName = "2.1.3"', g)
g = re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 213', g)
gradle.write_text(g, encoding='utf-8')

print('Added Studio OS v2.1.3 overlay, persistence, pipeline, registries, snapshots, diagnostics, and approved roadmap.')
