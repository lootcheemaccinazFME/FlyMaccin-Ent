from pathlib import Path
import re
import shutil

ROOT = Path("pocket-potna-v2.1.14")
APP = ROOT / "app/src/main"
EXPECTED_SIGNER = "93bcafc7947054445eea0bb54492a9067996b100648d0136a838c2e1a310adbb"

if not ROOT.exists():
    raise SystemExit("Pocket Potna source root missing")

# Approved FME visual assets.
(APP / "res/drawable-nodpi").mkdir(parents=True, exist_ok=True)
shutil.copyfile("build-assets/fme_v214_background.jpg", APP / "assets/fme_app_background.jpg")
shutil.copyfile("build-assets/fme_v214_background.jpg", APP / "res/drawable-nodpi/fme_app_background.jpg")
launcher_xml = APP / "res/drawable/ic_launcher.xml"
if launcher_xml.exists():
    launcher_xml.unlink()
shutil.copyfile("build-assets/fme_v214_icon.png", APP / "res/drawable/ic_launcher.png")

(APP / "res/drawable/app_window_background.xml").write_text(
    """<?xml version=\"1.0\" encoding=\"utf-8\"?>
<layer-list xmlns:android=\"http://schemas.android.com/apk/res/android\">
    <item>
        <bitmap android:src=\"@drawable/fme_app_background\" android:gravity=\"fill\" />
    </item>
    <item>
        <shape android:shape=\"rectangle\">
            <solid android:color=\"#66000000\" />
        </shape>
    </item>
</layer-list>
"""
)

# Version and UMD-030 fail-closed release signing.
gradle = ROOT / "app/build.gradle.kts"
s = gradle.read_text()
s = s.replace("versionCode = 223", "versionCode = 224")
s = s.replace('versionName = "2.1.13"', 'versionName = "2.1.14"')

old_signing = '''    signingConfigs {
        create("release") {
            val storePath = System.getenv("FME_KEYSTORE_PATH")
            if (!storePath.isNullOrBlank()) {
                storeFile = file(storePath)
                storePassword = System.getenv("FME_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("FME_KEY_ALIAS")
                keyPassword = System.getenv("FME_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug { isMinifyEnabled = false }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (!System.getenv("FME_KEYSTORE_PATH").isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
'''
new_signing = '''    val releaseTaskRequested = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
    fun requiredReleaseEnv(name: String): String {
        val value = System.getenv(name)?.trim().orEmpty()
        if (releaseTaskRequested && value.isBlank()) {
            throw GradleException("UMD-030 BLOCKED: missing required release signing input: $name")
        }
        return value
    }

    signingConfigs {
        create("release") {
            val storePath = requiredReleaseEnv("FME_KEYSTORE_PATH")
            if (releaseTaskRequested) {
                val keyFile = file(storePath)
                if (!keyFile.isFile) {
                    throw GradleException("UMD-030 BLOCKED: FME release keystore file is unavailable")
                }
                storeFile = keyFile
                storePassword = requiredReleaseEnv("FME_KEYSTORE_PASSWORD")
                keyAlias = requiredReleaseEnv("FME_KEY_ALIAS")
                keyPassword = requiredReleaseEnv("FME_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug { isMinifyEnabled = false }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
'''
if old_signing not in s:
    raise SystemExit("Expected signing block not found; refusing unsafe patch")
s = s.replace(old_signing, new_signing)
gradle.write_text(s)

# Native window background.
for rel in ["res/values/themes.xml", "res/values/persona_os_theme.xml"]:
    p = APP / rel
    t = p.read_text()
    if '<item name="android:windowBackground">' in t:
        t = re.sub(
            r'<item name="android:windowBackground">.*?</item>',
            '<item name="android:windowBackground">@drawable/app_window_background</item>',
            t,
        )
    else:
        t = t.replace(
            "</style>",
            '        <item name="android:windowBackground">@drawable/app_window_background</item>\n    </style>',
        )
    p.write_text(t)

# Main WebView and Bookwriter backgrounds.
main_css = APP / "assets/styles.css"
main_css.write_text(
    main_css.read_text()
    + '''

/* FME v2.1.14 approved Nulli Secundus app wallpaper */
html{background:#090a0d}
body{background-image:linear-gradient(rgba(5,3,10,.58),rgba(5,3,10,.78)),url("fme_app_background.jpg");background-position:center top;background-size:cover;background-repeat:no-repeat;background-attachment:fixed}
header{background:rgba(9,10,13,.88)}
nav{background:rgba(13,16,22,.92);backdrop-filter:blur(14px)}
.card{background:linear-gradient(180deg,rgba(23,27,36,.91),rgba(17,20,27,.91));backdrop-filter:blur(8px)}
.hero{background:radial-gradient(circle at 80% 0%,#40206b77,transparent 40%),linear-gradient(135deg,rgba(21,24,36,.90),rgba(13,17,24,.90));backdrop-filter:blur(8px)}
'''
)

book_css = APP / "assets/bookwriter/styles.css"
book_css.write_text(
    book_css.read_text()
    + '''

/* FME v2.1.14 approved Nulli Secundus app wallpaper */
body{background-image:linear-gradient(rgba(6,4,11,.72),rgba(6,4,11,.84)),url("../fme_app_background.jpg");background-position:center top;background-size:cover;background-repeat:no-repeat;background-attachment:fixed}
header{background:rgba(17,19,26,.92);backdrop-filter:blur(12px)}
nav{background:rgba(14,16,21,.92);backdrop-filter:blur(12px)}
.panel,.planCard,.stageCard,.card,.houseRoomCard,.housePatternCard{backdrop-filter:blur(8px)}
'''
)

# CreatorOS Compose shell background.
creator = APP / "java/com/flymaccin/creatoros/app/CreatorOsApp.kt"
t = creator.read_text()
t = t.replace(
    "import androidx.compose.foundation.background\n",
    "import androidx.compose.foundation.Image\nimport androidx.compose.foundation.background\n",
)
t = t.replace(
    "import androidx.compose.ui.Modifier\n",
    "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.layout.ContentScale\nimport androidx.compose.ui.res.painterResource\n",
)
t = t.replace(
    "import com.flymaccin.creatoros.CreatorOsApplication\n",
    "import com.flymaccin.creatoros.CreatorOsApplication\nimport com.flymaccin.bookwriter.R\n",
)
old_root = "        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D11))) {"
new_root = '''        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.fme_app_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.46f
            )
            Box(modifier = Modifier.fillMaxSize().background(Color(0x990D0D11)))
'''
if old_root not in t:
    raise SystemExit("CreatorOS root marker not found")
t = t.replace(old_root, new_root)
t = t.replace("containerColor = Color(0xFF0D0D11)", "containerColor = Color.Transparent", 1)
creator.write_text(t)

# Audit record.
(ROOT / "V2_1_14_CANONICAL_RELEASE_AUDIT.md").write_text(
    f"""# Pocket Potna v2.1.14 Canonical Release Audit

Status: UMD-030 ENFORCED

- versionCode: 224
- versionName: 2.1.14
- Approved FME background and launcher icon applied.
- ChatGPT OAuth/MCP and Keystore relay bridge preserved.
- Release tasks fail closed without the private FME release keystore and credentials.
- Canonical signer fingerprint: {EXPECTED_SIGNER}
"""
)

# Local deterministic sanity checks.
assert "versionCode = 224" in gradle.read_text()
assert 'versionName = "2.1.14"' in gradle.read_text()
assert "UMD-030 BLOCKED" in gradle.read_text()
assert "requestChatGPTConnectorCode" in (APP / "java/com/flymaccin/bookwriter/BookwriterBridge.java").read_text()
assert (APP / "res/drawable/ic_launcher.png").stat().st_size > 0
assert (APP / "assets/fme_app_background.jpg").stat().st_size > 0
print("Pocket Potna v2.1.14 release preparation PASS")
