from pathlib import Path
import re
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'demonic-ai-studio-hut')
assets = root / 'app/src/main/assets'
index = assets / 'index.html'
app_js = assets / 'app.js'
gradle = root / 'app/build.gradle.kts'

for p in (index, app_js, gradle):
    if not p.exists():
        raise SystemExit(f'Missing required file: {p}')

rooms_js = r'''(() => {
'use strict';
const STORE='demonic_rooms_v215';
const THEMES={
  royal:{name:'Royal Night',bg:'radial-gradient(circle at 15% 0%,#35204f 0,#17101f 34%,#09090d 72%,#050507 100%)',accent:'#c79cff',gold:'#d6ad55'},
  amber:{name:'Amber Fog',bg:'radial-gradient(circle at 80% 5%,#4a2c16 0,#17100c 38%,#08090b 78%,#050506 100%)',accent:'#e2b45d',gold:'#e2b45d'},
  midnight:{name:'Midnight Bay',bg:'radial-gradient(circle at 50% -10%,#17354a 0,#0d1720 38%,#07090c 75%,#040506 100%)',accent:'#79bde8',gold:'#c9a85a'}
};
const DEFAULT={theme:'royal',notes:[],art:[],files:[],gameBest:0};
let state=load();
let currentRoom=null, game=null, artCtx=null, artDrawing=false;
function load(){try{return Object.assign({},DEFAULT,JSON.parse(localStorage.getItem(STORE)||'{}'))}catch(_){return {...DEFAULT}}}
function save(){localStorage.setItem(STORE,JSON.stringify(state));}
function esc(s){return String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));}
function uid(){return Date.now().toString(36)+Math.random().toString(36).slice(2,7)}
function applyTheme(){const t=THEMES[state.theme]||THEMES.royal;document.documentElement.style.setProperty('--demonic-room-bg',t.bg);document.documentElement.style.setProperty('--demonic-room-accent',t.accent);document.documentElement.style.setProperty('--demonic-room-gold',t.gold);document.documentElement.dataset.demonicTheme=state.theme;document.body.dataset.demonicTheme=state.theme;document.body.style.background=t.bg;document.body.style.backgroundAttachment='fixed';}
function cycleTheme(){const keys=Object.keys(THEMES),i=keys.indexOf(state.theme);state.theme=keys[(i+1)%keys.length];save();applyTheme();toast('Background: '+THEMES[state.theme].name);}
function toast(msg){let e=document.getElementById('dr-toast');if(!e){e=document.createElement('div');e.id='dr-toast';document.body.appendChild(e)}e.textContent=msg;e.classList.add('show');clearTimeout(e._t);e._t=setTimeout(()=>e.classList.remove('show'),1500)}
function addLibrary(item){state.files.unshift(Object.assign({id:uid(),created:new Date().toISOString()},item));state.files=state.files.slice(0,120);save();}
function download(name,data,type='application/octet-stream'){const a=document.createElement('a');a.download=name;a.href=data.startsWith('data:')?data:URL.createObjectURL(new Blob([data],{type}));a.click();if(!data.startsWith('data:'))setTimeout(()=>URL.revokeObjectURL(a.href),1000)}

function shell(){let p=document.getElementById('dr-panel');if(p)return p;p=document.createElement('div');p.id='dr-panel';p.innerHTML='<div class="dr-shell"><header><div><b id="dr-title">Room</b><span>Demonic AI Studio Hut v2.1.5</span></div><button id="dr-close" aria-label="Close room">×</button></header><main id="dr-body"></main></div>';document.body.appendChild(p);p.querySelector('#dr-close').onclick=closeRoom;p.onclick=e=>{if(e.target===p)closeRoom()};return p;}
function openRoom(name){currentRoom=name;const p=shell();p.classList.add('open');const title={game:'Game Maker',art:'Art Room',notes:'Notes Room',library:'FME Library'}[name]||'Room';p.querySelector('#dr-title').textContent=title;renderRoom();}
function closeRoom(){stopGame();const p=document.getElementById('dr-panel');if(p)p.classList.remove('open');currentRoom=null}
function renderRoom(){const b=document.getElementById('dr-body');if(!b)return;if(currentRoom==='game')renderGame(b);else if(currentRoom==='art')renderArt(b);else if(currentRoom==='notes')renderNotes(b);else if(currentRoom==='library')renderLibrary(b)}

// PLAYABLE PINBALL + BASKETBALL GAME
function renderGame(b){b.innerHTML=`<section class="dr-card"><div class="dr-row"><div><h2>Pinball Basketball</h2><p>Launch the ball, keep it alive with the flippers, and score only by dropping it through the hoop.</p></div><div class="dr-score">SCORE <b id="game-score">0</b><small>BEST ${state.gameBest||0}</small></div></div><canvas id="game-canvas" width="720" height="1000" aria-label="Pinball basketball playfield"></canvas><div class="game-controls"><button id="fl-left">◀ LEFT FLIPPER</button><button id="game-launch">LAUNCH BALL</button><button id="fl-right">RIGHT FLIPPER ▶</button></div><div class="dr-actions"><button id="game-reset">New Game</button><button id="game-snap">Save Score to Library</button></div><p class="dr-hint">Touch and hold either flipper button. The ball relaunches after a drain, so the game does not die after one sad little gravity lesson.</p></section>`;
 const c=document.getElementById('game-canvas'),ctx=c.getContext('2d');
 game={canvas:c,ctx,score:0,lives:5,running:true,last:performance.now(),left:false,right:false,launched:false,scoredGate:false,ball:{x:640,y:860,vx:0,vy:0,r:18}};
 const bind=(id,key)=>{const el=document.getElementById(id);const down=e=>{e.preventDefault();game[key]=true};const up=e=>{e.preventDefault();if(game)game[key]=false};el.addEventListener('pointerdown',down);el.addEventListener('pointerup',up);el.addEventListener('pointercancel',up);el.addEventListener('pointerleave',up)};
 bind('fl-left','left');bind('fl-right','right');
 document.getElementById('game-launch').onclick=launchBall;
 document.getElementById('game-reset').onclick=()=>{stopGame();renderGame(b)};
 document.getElementById('game-snap').onclick=()=>{addLibrary({kind:'game',name:'Pinball Basketball score '+game.score,text:`Score: ${game.score} | Best: ${Math.max(state.gameBest||0,game.score)}`});toast('Game score saved to Library')};
 c.addEventListener('pointerdown',e=>{const r=c.getBoundingClientRect(),x=(e.clientX-r.left)*c.width/r.width;if(x<c.width/2)game.left=true;else game.right=true});
 c.addEventListener('pointerup',()=>{if(game){game.left=false;game.right=false}});
 requestAnimationFrame(gameLoop);
}
function launchBall(){if(!game||game.launched)return;game.ball.x=642;game.ball.y=850;game.ball.vx=-2.6-Math.random()*1.5;game.ball.vy=-15.5;game.launched=true;game.scoredGate=false}
function stopGame(){if(game)game.running=false;game=null}
function lineHit(ball,x1,y1,x2,y2,power){const dx=x2-x1,dy=y2-y1,l2=dx*dx+dy*dy;let t=((ball.x-x1)*dx+(ball.y-y1)*dy)/l2;t=Math.max(0,Math.min(1,t));const px=x1+t*dx,py=y1+t*dy,nx=ball.x-px,ny=ball.y-py,d=Math.hypot(nx,ny);if(d<ball.r+12&&d>0){const ux=nx/d,uy=ny/d;ball.x=px+ux*(ball.r+13);ball.y=py+uy*(ball.r+13);const dot=ball.vx*ux+ball.vy*uy;if(dot<0){ball.vx-=1.8*dot*ux;ball.vy-=1.8*dot*uy}ball.vy-=power;ball.vx+=(x2>x1?1:-1)*power*.35;return true}return false}
function bumper(ball,x,y,r){const dx=ball.x-x,dy=ball.y-y,d=Math.hypot(dx,dy);if(d<ball.r+r&&d>0){const ux=dx/d,uy=dy/d;ball.x=x+ux*(ball.r+r);ball.y=y+uy*(ball.r+r);const speed=Math.max(8,Math.hypot(ball.vx,ball.vy)*1.04);ball.vx=ux*speed;ball.vy=uy*speed;return true}return false}
function physics(dt){const g=game,b=g.ball,s=Math.min(2.2,dt/16.67);if(!g.launched)return;b.vy+=0.36*s;b.x+=b.vx*s;b.y+=b.vy*s;b.vx*=.999;
 if(b.x-b.r<24){b.x=24+b.r;b.vx=Math.abs(b.vx)*.88}if(b.x+b.r>696){b.x=696-b.r;b.vx=-Math.abs(b.vx)*.88}if(b.y-b.r<24){b.y=24+b.r;b.vy=Math.abs(b.vy)*.9}
 // backboard and hoop rims
 if(b.x+b.r>544&&b.x-b.r<568&&b.y>120&&b.y<320){b.x=544-b.r;b.vx=-Math.abs(b.vx)*.86}
 bumper(b,455,315,13);bumper(b,550,315,13);bumper(b,230,410,42);bumper(b,470,470,38);bumper(b,350,610,34);
 const la=g.left?-.48:.16,ra=g.right?Math.PI+.48:Math.PI-.16;
 const lx1=305,ly1=835,lx2=lx1-135*Math.cos(la),ly2=ly1-135*Math.sin(la);
 const rx1=415,ry1=835,rx2=rx1-135*Math.cos(ra),ry2=ry1-135*Math.sin(ra);
 lineHit(b,lx1,ly1,lx2,ly2,g.left?7:2);lineHit(b,rx1,ry1,rx2,ry2,g.right?7:2);
 // basket score: downward crossing through interior of rim
 if(!g.scoredGate&&b.vy>0&&b.y>300&&b.y<342&&b.x>472&&b.x<536){g.scoredGate=true;g.score+=2;state.gameBest=Math.max(state.gameBest||0,g.score);save();const s=document.getElementById('game-score');if(s)s.textContent=g.score;toast('BUCKET +2')}
 if(b.y>1040){g.lives--;g.launched=false;b.x=642;b.y=850;b.vx=b.vy=0;g.scoredGate=false;if(g.lives<=0){g.lives=5;toast('Ball rack reset. Keep shooting.')}else toast('Drain. '+g.lives+' balls left')}
}
function drawGame(){const g=game,ctx=g.ctx,c=g.canvas,b=g.ball;ctx.clearRect(0,0,c.width,c.height);const gr=ctx.createLinearGradient(0,0,0,c.height);gr.addColorStop(0,'#20102d');gr.addColorStop(.55,'#10121b');gr.addColorStop(1,'#08090c');ctx.fillStyle=gr;ctx.fillRect(0,0,c.width,c.height);ctx.strokeStyle='#d6ad55';ctx.lineWidth=5;ctx.strokeRect(24,24,672,952);
 // lane and court accents
 ctx.strokeStyle='#5d466f';ctx.lineWidth=3;ctx.beginPath();ctx.arc(505,315,105,0,Math.PI*2);ctx.stroke();ctx.fillStyle='#d6ad55';ctx.fillRect(550,145,16,165);ctx.strokeStyle='#f3ede3';ctx.lineWidth=6;ctx.beginPath();ctx.moveTo(458,315);ctx.lineTo(550,315);ctx.stroke();ctx.strokeStyle='#e07b66';ctx.lineWidth=8;ctx.beginPath();ctx.arc(503,315,48,0,Math.PI);ctx.stroke();
 [[230,410,42],[470,470,38],[350,610,34]].forEach(([x,y,r])=>{ctx.fillStyle='#39224d';ctx.strokeStyle='#c79cff';ctx.lineWidth=5;ctx.beginPath();ctx.arc(x,y,r,0,Math.PI*2);ctx.fill();ctx.stroke()});
 const la=g.left?-.48:.16,ra=g.right?Math.PI+.48:Math.PI-.16;ctx.strokeStyle='#d6ad55';ctx.lineCap='round';ctx.lineWidth=24;ctx.beginPath();ctx.moveTo(305,835);ctx.lineTo(305-135*Math.cos(la),835-135*Math.sin(la));ctx.moveTo(415,835);ctx.lineTo(415-135*Math.cos(ra),835-135*Math.sin(ra));ctx.stroke();ctx.lineCap='butt';
 ctx.fillStyle='#f08b44';ctx.strokeStyle='#f6c18f';ctx.lineWidth=3;ctx.beginPath();ctx.arc(b.x,b.y,b.r,0,Math.PI*2);ctx.fill();ctx.stroke();ctx.strokeStyle='#8a451f';ctx.lineWidth=2;ctx.beginPath();ctx.arc(b.x,b.y,b.r*.8,-.9,.9);ctx.moveTo(b.x-b.r,b.y);ctx.lineTo(b.x+b.r,b.y);ctx.stroke();
 ctx.fillStyle='#fff';ctx.font='bold 24px sans-serif';ctx.fillText('BALLS '+g.lives,40,65);if(!g.launched){ctx.font='bold 28px sans-serif';ctx.fillText('TAP LAUNCH BALL',455,900)} }
function gameLoop(now){if(!game||!game.running)return;const dt=now-game.last;game.last=now;physics(dt);drawGame();requestAnimationFrame(gameLoop)}

// ART ROOM
function renderArt(b){b.innerHTML=`<section class="dr-card"><h2>Art Room</h2><p>Draw directly on the canvas. Art can be saved into the local FME Library or exported as PNG.</p><div class="art-tools"><label>Color <input id="art-color" type="color" value="#d6ad55"></label><label>Brush <input id="art-size" type="range" min="2" max="48" value="8"></label><button id="art-clear">Clear</button><button id="art-save">Save to Library</button><button id="art-export">Export PNG</button></div><canvas id="art-canvas" width="1000" height="1000"></canvas></section>`;const c=document.getElementById('art-canvas');artCtx=c.getContext('2d');artCtx.fillStyle='#111119';artCtx.fillRect(0,0,c.width,c.height);artCtx.lineCap='round';artCtx.lineJoin='round';const pos=e=>{const r=c.getBoundingClientRect();return[(e.clientX-r.left)*c.width/r.width,(e.clientY-r.top)*c.height/r.height]};c.onpointerdown=e=>{artDrawing=true;c.setPointerCapture(e.pointerId);const [x,y]=pos(e);artCtx.beginPath();artCtx.moveTo(x,y)};c.onpointermove=e=>{if(!artDrawing)return;const [x,y]=pos(e);artCtx.strokeStyle=document.getElementById('art-color').value;artCtx.lineWidth=+document.getElementById('art-size').value;artCtx.lineTo(x,y);artCtx.stroke()};c.onpointerup=c.onpointercancel=()=>artDrawing=false;document.getElementById('art-clear').onclick=()=>{artCtx.fillStyle='#111119';artCtx.fillRect(0,0,c.width,c.height)};document.getElementById('art-save').onclick=()=>{const data=c.toDataURL('image/png');const name='Artwork '+new Date().toLocaleString();state.art.unshift({id:uid(),name,data,created:new Date().toISOString()});state.art=state.art.slice(0,20);addLibrary({kind:'art',name,data});save();toast('Artwork saved to Library')};document.getElementById('art-export').onclick=()=>download('Demonic_Art_'+Date.now()+'.png',c.toDataURL('image/png'),'image/png')}

// NOTES ROOM
function renderNotes(b){if(!state.notes.length)state.notes.push({id:uid(),title:'New Note',body:'',updated:new Date().toISOString()});save();const active=state.notes[0];b.innerHTML=`<section class="dr-card"><div class="dr-row"><div><h2>Notes Room</h2><p>Persistent notes stay on this device between app launches.</p></div><button id="note-new">+ New Note</button></div><div class="notes-grid"><aside id="note-list">${state.notes.map((n,i)=>`<button data-note="${n.id}" class="${i===0?'active':''}"><b>${esc(n.title||'Untitled')}</b><small>${new Date(n.updated).toLocaleString()}</small></button>`).join('')}</aside><div><input id="note-title" value="${esc(active.title)}" placeholder="Note title"><textarea id="note-body" placeholder="Write here...">${esc(active.body)}</textarea><div class="dr-actions"><button id="note-save">Save</button><button id="note-library">Save Copy to Library</button><button id="note-delete">Delete</button></div></div></div></section>`;let selected=active.id;const loadNote=id=>{const n=state.notes.find(x=>x.id===id);if(!n)return;selected=id;document.getElementById('note-title').value=n.title;document.getElementById('note-body').value=n.body;document.querySelectorAll('[data-note]').forEach(x=>x.classList.toggle('active',x.dataset.note===id))};const commit=()=>{const n=state.notes.find(x=>x.id===selected);if(!n)return;n.title=document.getElementById('note-title').value||'Untitled';n.body=document.getElementById('note-body').value;n.updated=new Date().toISOString();state.notes.sort((a,z)=>new Date(z.updated)-new Date(a.updated));save();toast('Note saved')};document.querySelectorAll('[data-note]').forEach(x=>x.onclick=()=>loadNote(x.dataset.note));document.getElementById('note-new').onclick=()=>{state.notes.unshift({id:uid(),title:'New Note',body:'',updated:new Date().toISOString()});save();renderNotes(b)};document.getElementById('note-save').onclick=()=>{commit();renderNotes(b)};document.getElementById('note-library').onclick=()=>{commit();const n=state.notes.find(x=>x.id===selected);addLibrary({kind:'note',name:n.title,text:n.body});toast('Note copied to Library')};document.getElementById('note-delete').onclick=()=>{if(state.notes.length===1){state.notes[0]={id:uid(),title:'New Note',body:'',updated:new Date().toISOString()}}else state.notes=state.notes.filter(x=>x.id!==selected);save();renderNotes(b)};document.getElementById('note-title').oninput=commit;document.getElementById('note-body').oninput=commit}

// FUNCTIONAL LOCAL LIBRARY
function renderLibrary(b){b.innerHTML=`<section class="dr-card"><div class="dr-row"><div><h2>FME Library</h2><p>Saved art, notes, game records, and imported local files.</p></div><label class="dr-import">Import File<input id="lib-import" type="file"></label></div><div id="lib-list">${state.files.map(f=>`<article class="lib-item"><div><b>${esc(f.name)}</b><small>${esc(f.kind)} • ${new Date(f.created).toLocaleString()}</small></div>${f.kind==='art'&&f.data?`<img src="${f.data}" alt="${esc(f.name)}">`:''}${f.text?`<pre>${esc(f.text.slice(0,1200))}</pre>`:''}<div class="dr-actions">${f.data?`<button data-export="${f.id}">Export</button>`:''}${f.text?`<button data-text="${f.id}">Export Text</button>`:''}<button data-delete="${f.id}">Delete</button></div></article>`).join('')||'<div class="dr-empty">Library is empty. Save a note, artwork, game score, or import a file.</div>'}</div></section>`;document.getElementById('lib-import').onchange=e=>{const file=e.target.files[0];if(!file)return;if(file.size>3*1024*1024){toast('Import limit is 3 MB per local file');return}const r=new FileReader();r.onload=()=>{addLibrary({kind:'import',name:file.name,data:r.result,mime:file.type||'application/octet-stream'});renderLibrary(b)};r.readAsDataURL(file)};b.querySelectorAll('[data-delete]').forEach(x=>x.onclick=()=>{state.files=state.files.filter(f=>f.id!==x.dataset.delete);save();renderLibrary(b)});b.querySelectorAll('[data-export]').forEach(x=>x.onclick=()=>{const f=state.files.find(v=>v.id===x.dataset.export);if(f)download(f.name.replace(/[^a-z0-9._-]/gi,'_')+(f.kind==='art'?'.png':''),f.data,f.mime)});b.querySelectorAll('[data-text]').forEach(x=>x.onclick=()=>{const f=state.files.find(v=>v.id===x.dataset.text);if(f)download((f.name||'note').replace(/[^a-z0-9._-]/gi,'_')+'.txt',f.text||'','text/plain')})}

function addLaunchBar(){if(document.getElementById('dr-launchbar'))return;const bar=document.createElement('nav');bar.id='dr-launchbar';bar.setAttribute('aria-label','Studio rooms');bar.innerHTML='<button data-room="game">🏀 Game</button><button data-room="art">🎨 Art</button><button data-room="notes">📝 Notes</button><button data-room="library">📚 Library</button><button id="dr-theme">◐ BG</button>';document.body.appendChild(bar);bar.querySelectorAll('[data-room]').forEach(x=>x.onclick=()=>openRoom(x.dataset.room));bar.querySelector('#dr-theme').onclick=cycleTheme}
function hookRouting(){let tries=0;const t=setInterval(()=>{tries++;try{if(window.PP&&typeof window.PP.nav==='function'&&!window.PP.__rooms215){const orig=window.PP.nav.bind(window.PP);window.PP.nav=function(r,...args){if(['game','art','notes','library'].includes(r)){openRoom(r);return}return orig(r,...args)};window.PP.__rooms215=true;clearInterval(t)}}catch(_){}if(tries>80)clearInterval(t)},100)}
function mount(){applyTheme();shell();addLaunchBar();hookRouting();window.DemonicRooms={open:openRoom,theme:cycleTheme,getState:()=>JSON.parse(JSON.stringify(state)),version:'2.1.5'};}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',mount);else mount();
})();
'''

rooms_css = r''':root{--demonic-room-bg:radial-gradient(circle at 15% 0%,#35204f 0,#17101f 34%,#09090d 72%,#050507 100%);--demonic-room-accent:#c79cff;--demonic-room-gold:#d6ad55}html,body{min-height:100%;background:var(--demonic-room-bg)!important;background-attachment:fixed!important}body::before{content:"";position:fixed;inset:0;z-index:-2;background:var(--demonic-room-bg);pointer-events:none}#app,.app,.shell,.page,.content{background-color:transparent!important}#dr-launchbar{position:fixed;left:50%;bottom:8px;transform:translateX(-50%);z-index:2147482500;display:flex;gap:5px;padding:6px;background:#08090de8;border:1px solid #3d3448;border-radius:14px;box-shadow:0 8px 28px #000a;backdrop-filter:blur(9px)}#dr-launchbar button{border:1px solid #403753;background:#18131f;color:#f7f4fa;border-radius:9px;padding:8px 9px;font:700 11px/1 sans-serif;white-space:nowrap}#dr-launchbar button:active{transform:translateY(1px);background:#302343}#dr-panel{display:none;position:fixed;inset:0;z-index:2147482900;background:#000b;padding:8px;box-sizing:border-box}#dr-panel.open{display:block}.dr-shell{height:calc(100vh - 16px);max-width:1050px;margin:auto;display:flex;flex-direction:column;background:#0a0b0feF;color:#f5f2f8;border:1px solid #403753;border-radius:18px;overflow:hidden;box-shadow:0 20px 80px #000}.dr-shell>header{display:flex;align-items:center;justify-content:space-between;padding:12px 15px;background:#121019;border-bottom:1px solid #302a38}.dr-shell>header div{display:flex;flex-direction:column}.dr-shell>header span{font-size:10px;color:#9993a0}.dr-shell>header button{border:0;background:transparent;color:#fff;font-size:30px}.dr-shell>main{overflow:auto;padding:12px;padding-bottom:82px}.dr-card{background:#101117;border:1px solid #292b34;border-radius:14px;padding:12px}.dr-card h2{margin:0 0 5px}.dr-card p{color:#aaa5b1;font-size:12px}.dr-row{display:flex;align-items:center;justify-content:space-between;gap:12px}.dr-score{text-align:right;font-size:11px;color:#aaa}.dr-score b{display:block;font-size:32px;color:var(--demonic-room-gold)}.dr-score small{display:block}.dr-actions,.art-tools,.game-controls{display:flex;gap:7px;flex-wrap:wrap;margin:10px 0}.dr-card button,.dr-import{background:#201a29;color:#f4f0f6;border:1px solid #463b57;border-radius:9px;padding:9px 11px;font:inherit}.game-controls{display:grid;grid-template-columns:1fr 1.2fr 1fr}.game-controls button{font-weight:800;min-height:48px}.dr-card canvas{display:block;max-width:100%;height:auto;margin:10px auto;background:#111119;border:1px solid #4c3d5d;border-radius:12px;touch-action:none}#game-canvas{max-height:62vh}.art-tools{align-items:center}.art-tools label{display:flex;align-items:center;gap:6px;font-size:12px}.notes-grid{display:grid;grid-template-columns:220px 1fr;gap:10px}#note-list{display:flex;flex-direction:column;gap:5px;max-height:60vh;overflow:auto}#note-list button{text-align:left;display:flex;flex-direction:column}#note-list button.active{border-color:var(--demonic-room-accent);background:#302343}#note-list small,.lib-item small{color:#918c99;font-size:10px}#note-title,#note-body{width:100%;box-sizing:border-box;background:#090a0d;color:#fff;border:1px solid #363440;border-radius:9px;padding:10px}#note-body{min-height:45vh;margin-top:8px;resize:vertical}.dr-import input{display:none}.lib-item{display:grid;grid-template-columns:1fr auto;gap:8px;margin:8px 0;padding:10px;border:1px solid #292b34;border-radius:11px;background:#0c0d12}.lib-item>div:first-child{display:flex;flex-direction:column}.lib-item img{grid-row:span 2;width:90px;height:90px;object-fit:cover;border-radius:8px}.lib-item pre{grid-column:1/-1;white-space:pre-wrap;max-height:140px;overflow:auto;color:#c7c2cc;font-size:11px}.dr-empty{padding:22px;text-align:center;color:#8f8996}.dr-hint{font-size:11px!important}#dr-toast{position:fixed;left:50%;bottom:78px;transform:translate(-50%,20px);z-index:2147483600;background:#1e1728;color:#fff;border:1px solid var(--demonic-room-accent);padding:9px 13px;border-radius:10px;opacity:0;pointer-events:none;transition:.18s}#dr-toast.show{opacity:1;transform:translate(-50%,0)}@media(max-width:680px){#dr-launchbar{width:calc(100vw - 14px);justify-content:space-between}#dr-launchbar button{padding:8px 7px}.notes-grid{grid-template-columns:1fr}#note-list{max-height:140px}.dr-row{align-items:flex-start}.game-controls{grid-template-columns:1fr 1fr}.game-controls #game-launch{grid-column:1/-1;grid-row:1}.dr-shell>main{padding:8px;padding-bottom:86px}}
'''

(assets / 'rooms_v215.js').write_text(rooms_js, encoding='utf-8')
(assets / 'rooms_v215.css').write_text(rooms_css, encoding='utf-8')

html = index.read_text(encoding='utf-8')
if 'rooms_v215.css' not in html:
    html = html.replace('</head>', '<link rel="stylesheet" href="rooms_v215.css">\n</head>')
if 'rooms_v215.js' not in html:
    html = html.replace('</body>', '<script src="rooms_v215.js"></script>\n</body>')
index.write_text(html, encoding='utf-8')

# Add Art and Notes to the native app nav array when the known Game Maker tuple is present.
js = app_js.read_text(encoding='utf-8')
if "['art','Art Room']" not in js:
    js2, n = re.subn(r"(\['game','Game Maker'\])", "['art','Art Room'],['notes','Notes Room'],\\1", js, count=1)
    if n:
        js = js2
app_js.write_text(js, encoding='utf-8')

g = gradle.read_text(encoding='utf-8')
g = re.sub(r'versionName\s*=\s*"2\.1\.4"', 'versionName = "2.1.5"', g)
g = re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 215', g)
gradle.write_text(g, encoding='utf-8')

# Fail patching if the actual functional artifacts were not created.
checks = {
    'rooms_v215.js':['Pinball Basketball','requestAnimationFrame(gameLoop)','function renderArt','function renderNotes','function renderLibrary','demonic_rooms_v215','cycleTheme'],
    'rooms_v215.css':['--demonic-room-bg','#dr-launchbar','#dr-panel'],
}
for name, needles in checks.items():
    text=(assets/name).read_text(encoding='utf-8')
    miss=[x for x in needles if x not in text]
    if miss: raise SystemExit(f'{name} missing functional markers: {miss}')
print('Applied Demonic AI Studio Hut v2.1.5 functional rooms, playable game, persistent notes/art/library, and visible backgrounds.')
