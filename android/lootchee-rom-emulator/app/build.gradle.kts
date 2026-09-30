plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.flymaccin.lootcheerom"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.flymaccin.lootcheerom"; minSdk = 26; targetSdk = 36; versionCode = 1; versionName = "0.2.0"
        externalNativeBuild { cmake { cppFlags += "-std=c++17" } }
        ndk { abiFilters += listOf("arm64-v8a") }
    }
    externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt") } }
    sourceSets["main"].jniLibs.srcDir("src/main/jniLibs")
}
dependencies { implementation("androidx.core:core-ktx:1.15.0"); implementation("androidx.appcompat:appcompat:1.7.0") }
