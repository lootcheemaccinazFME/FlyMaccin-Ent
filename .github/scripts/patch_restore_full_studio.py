from pathlib import Path
import re
import shutil
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'demonic-ai-studio-hut')
repo_assets = Path('app/src/main/assets')
assets = root / 'app/src/main/assets'
app_js = assets / 'app.js'
styles = assets / 'styles.css'
main = root / 'app/src/main/java/com/flymaccin/bookwriter/MainActivity.java'
gradle = root / 'app/build.gradle.kts'

if not app_js.exists():
    raise SystemExit(f'Missing unified app.js: {app_js}')
if not repo_assets.exists():
    raise SystemExit(f'Missing checked-out Book Writer assets: {repo_assets}')

# Preserve the complete pre-existing FME Bookwriter as a nested first-class room.
bookwriter_dir = assets / 'bookwriter'
if bookwriter_dir.exists():
    shutil.rmtree(bookwriter_dir)
shutil.copytree(repo_assets, bookwriter_dir)

js = app_js.read_text(encoding='utf-8')

old_nav = "['home','Home'],['creator','Creator AI'],['daw','Demonic DAW'],['scripture','Scripture Writer']"
new_nav = "['home','Home'],['bookwriter','Book Writer'],['creator','Creator AI'],['daw','Demonic DAW'],['scripture','Scripture Writer']"
if old_nav not in js:
    raise SystemExit('Unified nav signature not found; refusing unsafe patch.')
js = js.replace(old_nav, new_nav, 1)

home_anchor = "<div class=\"grid section\"><div class=\"card tap\" onclick=\"PP.nav('creator')\">"
if home_anchor not in js:
    raise SystemExit('Home grid anchor not found.')
book_card = "<div class=\"grid section\"><div class=\"card tap\" onclick=\"PP.nav('bookwriter')\"><div class=\"kicker\">LONGFORM WRITING</div><div class=\"title\">Book Writer</div><div class=\"desc\">Full FME Bookwriter: planning, drafting, research, Story Bible, projects, versions, houses, and AI Studios.</div></div><div class=\"card tap\" onclick=\"PP.nav('creator')\">"
js = js.replace(home_anchor, book_card, 1)

book_func = """
function bookWriterPage(){return `<div class="section"><h1>Book Writer</h1><div class="muted">Full FME Bookwriter merged into Demonic AI Studio Hut. This is the complete existing writer, not a reduced scripture form.</div></div><div class="card section bookwriterHost"><iframe class="bookwriterFrame" src="bookwriter/index.html" title="FME Bookwriter OpenAI Studios"></iframe></div>`}
"""
scripture_marker = 'function scripturePage()'
if scripture_marker not in js:
    raise SystemExit('Scripture marker not found.')
js = js.replace(scripture_marker, book_func + scripture_marker, 1)

old_render = "route==='home'?home():route==='creator'?creatorAIPage():route==='daw'?dawPage():route==='scripture'?scripturePage()"
new_render = "route==='home'?home():route==='bookwriter'?bookWriterPage():route==='creator'?creatorAIPage():route==='daw'?dawPage():route==='scripture'?scripturePage()"
if old_render not in js:
    raise SystemExit('Render route signature not found.')
js = js.replace(old_render, new_render, 1)

old_allowed = "['home','creator','daw','scripture','game','development','beatmaker','tools','library','audit','settings']"
new_allowed = "['home','bookwriter','creator','daw','scripture','game','development','beatmaker','tools','library','audit','settings']"
if old_allowed in js:
    js = js.replace(old_allowed, new_allowed)

js = js.replace("version:'2.1.0-pocket-potna-fme'", "version:'2.1.2-demonic-full-studio'")
app_js.write_text(js, encoding='utf-8')

css = styles.read_text(encoding='utf-8') if styles.exists() else ''
extra_css = "\n.bookwriterHost{padding:0;overflow:hidden}.bookwriterFrame{display:block;width:100%;height:calc(100vh - 150px);min-height:720px;border:0;background:#0a0b0f}\n"
if '.bookwriterFrame{' not in css:
    styles.write_text(css + extra_css, encoding='utf-8')

# Allow the nested local Book Writer document while still blocking external WebView navigation.
mt = main.read_text(encoding='utf-8')
mt = mt.replace('&& "/android_asset/index.html".equals(u.getPath());', '&& u.getPath() != null\n                        && u.getPath().startsWith("/android_asset/");')
mt = mt.replace('return !TRUSTED_URL.equals(url);', 'return !(url != null && url.startsWith("file:///android_asset/"));')
main.write_text(mt, encoding='utf-8')

# Final merged build version.
g = gradle.read_text(encoding='utf-8')
g = re.sub(r'versionName\s*=\s*"2\.1\.1"', 'versionName = "2.1.2"', g)
g = re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 212', g)
gradle.write_text(g, encoding='utf-8')

required = [
    'Book Writer', 'Creator AI', 'Demonic DAW', 'Scripture Writer', 'Game Maker',
    'Development', 'Beat Maker', 'Tools', 'Library', 'Audit', 'Settings'
]
final_js = app_js.read_text(encoding='utf-8')
missing = [x for x in required if x not in final_js]
if missing:
    raise SystemExit('Missing merged rooms: ' + ', '.join(missing))
for name in ['index.html', 'app.js', 'core.mjs', 'houses.js', 'styles.css']:
    if not (bookwriter_dir / name).exists():
        raise SystemExit(f'Missing Book Writer asset after merge: {name}')

print('Merged full FME Bookwriter + unified Demonic/Pocket Potna studio as v2.1.2.')
