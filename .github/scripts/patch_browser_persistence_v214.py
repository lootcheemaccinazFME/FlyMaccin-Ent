from pathlib import Path
import re
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'demonic-ai-studio-hut')
browser = root / 'app/src/main/java/com/flymaccin/bookwriter/BrowserActivity.java'
gradle = root / 'app/build.gradle.kts'

if not browser.exists():
    raise SystemExit('Missing BrowserActivity.java')

src = browser.read_text(encoding='utf-8')

# Ensure the browser can explicitly manage persistent WebView cookies.
if 'import android.webkit.CookieManager;' not in src:
    src = src.replace('import android.webkit.', 'import android.webkit.CookieManager;\nimport android.webkit.', 1)

# Find the BrowserActivity WebView variable name from a getSettings() call.
match = re.search(r'([A-Za-z_][A-Za-z0-9_]*)\.getSettings\(\)', src)
if not match:
    raise SystemExit('Could not locate BrowserActivity WebView settings call')
webview = match.group(1)

# Enable normal persistent browser storage and OAuth/login cookies.
marker = '// DEMONIC_BROWSER_PERSISTENCE_V214'
if marker not in src:
    settings_call = f'{webview}.getSettings();'
    pos = src.find(settings_call)
    if pos < 0:
        raise SystemExit('Could not locate WebView getSettings() statement')
    insert_at = pos + len(settings_call)
    persistence = f'''\n        {marker}\n        CookieManager cookieManager = CookieManager.getInstance();\n        cookieManager.setAcceptCookie(true);\n        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {{\n            cookieManager.setAcceptThirdPartyCookies({webview}, true);\n        }}\n        {webview}.getSettings().setDomStorageEnabled(true);\n        {webview}.getSettings().setDatabaseEnabled(true);\n        {webview}.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);\n'''
    src = src[:insert_at] + persistence + src[insert_at:]

# Remove browser-exit/session cleanup that would defeat "remember me" behavior.
src = re.sub(r'(?m)^\s*CookieManager\.getInstance\(\)\.removeAllCookies\([^;]*;\s*$', '', src)
src = re.sub(r'(?m)^\s*CookieManager\.getInstance\(\)\.removeSessionCookies\([^;]*;\s*$', '', src)
src = re.sub(rf'(?m)^\s*{re.escape(webview)}\.clearCache\(true\);\s*$', '', src)
src = re.sub(rf'(?m)^\s*{re.escape(webview)}\.clearFormData\(\);\s*$', '', src)

# Flush cookie state at lifecycle boundaries so persistent login cookies survive app restarts.
flush_method = '''\n    private void flushBrowserSession() {\n        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {\n            CookieManager.getInstance().flush();\n        }\n    }\n'''
if 'private void flushBrowserSession()' not in src:
    class_end = src.rfind('}')
    if class_end < 0:
        raise SystemExit('Malformed BrowserActivity.java')
    src = src[:class_end] + flush_method + '\n' + src[class_end:]

# Add lifecycle flush hooks only if the activity does not already define them.
def add_lifecycle(method_name, body):
    global src
    if re.search(rf'\bvoid\s+{method_name}\s*\(', src):
        return
    class_end = src.rfind('}')
    block = f'''\n    @Override protected void {method_name}() {{\n        {body}\n    }}\n'''
    src = src[:class_end] + block + '\n' + src[class_end:]

add_lifecycle('onPause', 'flushBrowserSession();\n        super.onPause();')
add_lifecycle('onStop', 'flushBrowserSession();\n        super.onStop();')

# If onDestroy exists, inject a flush before super.onDestroy(). Otherwise create it.
if re.search(r'\bvoid\s+onDestroy\s*\(', src):
    if 'flushBrowserSession();' not in re.search(r'(?s)(@Override\s+)?[^\n]*void\s+onDestroy\s*\([^)]*\)\s*\{.*?\n\s*\}', src).group(0):
        src = re.sub(r'(\bvoid\s+onDestroy\s*\([^)]*\)\s*\{)', r'\1\n        flushBrowserSession();', src, count=1)
else:
    add_lifecycle('onDestroy', 'flushBrowserSession();\n        super.onDestroy();')

browser.write_text(src, encoding='utf-8')

g = gradle.read_text(encoding='utf-8')
g = re.sub(r'versionName\s*=\s*"2\.1\.3"', 'versionName = "2.1.4"', g)
g = re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 214', g)
gradle.write_text(g, encoding='utf-8')

print(f'Enabled persistent browser sessions in {browser.name}; version set to 2.1.4.')
