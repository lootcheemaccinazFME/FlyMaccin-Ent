from pathlib import Path
import re
import shutil
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'demonic-ai-studio-hut')
repo_assets = Path('app/src/main/assets')
assets = root / 'app/src/main/assets'
index = assets / 'index.html'
app_js = assets / 'app.js'
studio = assets / 'studio_os.js'
gradle = root / 'app/build.gradle.kts'

for p in (index, app_js, studio, gradle, repo_assets / 'functional_v216.js', repo_assets / 'functional_v216.css'):
    if not p.exists():
        raise SystemExit(f'Missing required file: {p}')

shutil.copy2(repo_assets / 'functional_v216.js', assets / 'functional_v216.js')
shutil.copy2(repo_assets / 'functional_v216.css', assets / 'functional_v216.css')

html = index.read_text(encoding='utf-8')
if 'functional_v216.css' not in html:
    html = html.replace('</head>', '<link rel="stylesheet" href="functional_v216.css">\n</head>')
if 'functional_v216.js' not in html:
    html = html.replace('</body>', '<script src="functional_v216.js"></script>\n</body>')
index.write_text(html, encoding='utf-8')

js = app_js.read_text(encoding='utf-8')
js = js.replace("version:'2.1.2-demonic-full-studio'", "version:'2.1.6-functional-core'")
app_js.write_text(js, encoding='utf-8')

s = studio.read_text(encoding='utf-8')
s = s.replace("['Semantic FME Library search','Approved / indexing engine pending']", "['Semantic FME Library search','Functional local relevance search implemented']")
s = s.replace("['Agent handoffs','Pipeline routing implemented; autonomous agents pending']", "['Agent handoffs','Functional persisted handoff queue + room routing']")
s = s.replace("['Visual Studio generation/editing/segmentation','Approved / generation engines pending']", "['Visual Studio generation/editing/segmentation','Local image editing/export implemented; model generation remains provider-dependent']")
s = s.replace("['Natural-language orchestration','Command templates implemented; autonomous orchestration pending']", "['Natural-language orchestration','Functional local command router implemented; model orchestration remains optional']")
s = s.replace("['Beat Maker sequencer/piano roll/sampler/808/chop','Approved / deeper engine pending']", "['Beat Maker sequencer/piano roll/sampler/808/chop','16-step local kick/snare/hat WAV renderer implemented; deeper sampler/chop pending']")
studio.write_text(s, encoding='utf-8')

g = gradle.read_text(encoding='utf-8')
g = re.sub(r'versionName\s*=\s*"2\.1\.5"', 'versionName = "2.1.6"', g)
g = re.sub(r'versionCode\s*=\s*215', 'versionCode = 216', g)
gradle.write_text(g, encoding='utf-8')

print('Applied Demonic Functional Core v2.1.6: local search, handoffs, command routing, WAV beat rendering, image editing, self-tests, backup/import.')
