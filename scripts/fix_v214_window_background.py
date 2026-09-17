from pathlib import Path

ROOT = Path("pocket-potna-v2.1.14")
APP = ROOT / "app/src/main"
WINDOW_BG = APP / "res/drawable/app_window_background.xml"
FME_IMAGE = APP / "res/drawable-nodpi/fme_app_background.jpg"

if not ROOT.exists():
    raise SystemExit("Pocket Potna source root missing")
if not FME_IMAGE.is_file() or FME_IMAGE.stat().st_size == 0:
    raise SystemExit("FME background image missing")

# Android was crashing while inflating the JPEG through a <bitmap> inside the
# theme windowBackground before MainActivity could stay alive. Keep the FME
# image in the real app surfaces (WebView + CreatorOS Compose), but make the
# native startup/window drawable a resource-safe dark FME fallback.
WINDOW_BG.parent.mkdir(parents=True, exist_ok=True)
WINDOW_BG.write_text(
    """<?xml version=\"1.0\" encoding=\"utf-8\"?>
<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"
    android:shape=\"rectangle\">
    <solid android:color=\"#090A0D\" />
</shape>
""",
    encoding="utf-8",
)

text = WINDOW_BG.read_text(encoding="utf-8")
if "<bitmap" in text or "@drawable/fme_app_background" in text:
    raise SystemExit("Unsafe bitmap-backed native window background still present")
if "#090A0D" not in text:
    raise SystemExit("Safe FME startup fallback was not written")

print("Pocket Potna v2.1.14 native window background crash fix PASS")
