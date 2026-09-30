plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.flymaccin.lootcheerom"
    compileSdk = 36
    defaultConfig { applicationId = "com.flymaccin.lootcheerom"; minSdk = 26; targetSdk = 36; versionCode = 1; versionName = "0.1.0" }
}
dependencies { implementation("androidx.core:core-ktx:1.15.0"); implementation("androidx.appcompat:appcompat:1.7.0") }
