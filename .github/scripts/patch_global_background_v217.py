from pathlib import Path
import re
import shutil
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'demonic-ai-studio-hut')
repo_assets = Path('app/src/main/assets')
assets = root / 'app/src/main/assets'
index = assets / 'index.html'
bookwriter_index = assets / 'bookwriter/index.html'
gradle = root / 'app/build.gradle.kts'

background = repo_assets / 'fme_qr_hub_bg.jpg'
css = repo_assets / 'global_background_v217.css'

for p in (index, bookwriter_index, gradle, background, css):
    if not p.exists():
        raise SystemExit(f'Missing required file: {p}')

shutil.copy2(background, assets / 'fme_qr_hub_bg.jpg')
shutil.copy2(css, assets / 'global_background_v217.css')

html = index.read_text(encoding='utf-8')
if 'global_background_v217.css' not in html:
    html = html.replace('</head>', '<link rel="stylesheet" href="global_background_v217.css">\n</head>')
index.write_text(html, encoding='utf-8')

book_html = bookwriter_index.read_text(encoding='utf-8')
if 'global_background_v217.css' not in book_html:
    book_html = book_html.replace('</head>', '<link rel="stylesheet" href="../global_background_v217.css">\n</head>')
bookwriter_index.write_text(book_html, encoding='utf-8')

g = gradle.read_text(encoding='utf-8')
g = re.sub(r'versionName\s*=\s*"2\.1\.6"', 'versionName = "2.1.7"', g)
g = re.sub(r'versionCode\s*=\s*216', 'versionCode = 217', g)
gradle.write_text(g, encoding='utf-8')

print('Applied FME QR Hub global background v2.1.7 to main studio and Book Writer.')
