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

css = repo_assets / 'global_background_v218.css'
background = repo_assets / 'fme_qr_hub_bg.webp'

for p in (index, bookwriter_index, gradle, css, background):
    if not p.exists():
        raise SystemExit(f'Missing required file: {p}')

# Copy the verified image binary directly into Android assets.
shutil.copy2(background, assets / 'fme_qr_hub_bg.webp')
shutil.copy2(css, assets / 'global_background_v218.css')

html = index.read_text(encoding='utf-8')
if 'global_background_v218.css' not in html:
    html = html.replace('</head>', '<link rel="stylesheet" href="global_background_v218.css">\n</head>')
index.write_text(html, encoding='utf-8')

book_html = bookwriter_index.read_text(encoding='utf-8')
if 'global_background_v218.css' not in book_html:
    book_html = book_html.replace('</head>', '<link rel="stylesheet" href="../global_background_v218.css">\n</head>')
bookwriter_index.write_text(book_html, encoding='utf-8')

g = gradle.read_text(encoding='utf-8')
g = re.sub(r'versionName\s*=\s*"2\.1\.7"', 'versionName = "2.1.8"', g)
g = re.sub(r'versionCode\s*=\s*217', 'versionCode = 218', g)
gradle.write_text(g, encoding='utf-8')

print('Applied v2.1.8 visible global background using verified WebP asset to main studio and Book Writer.')
