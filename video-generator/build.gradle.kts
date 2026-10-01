plugins { id("com.android.application") }
android {
 namespace = "com.flymaccin.videogenerator"
 compileSdk = 35
 defaultConfig { applicationId = "com.flymaccin.videogenerator"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
 buildTypes { debug { isMinifyEnabled = false }; release { isMinifyEnabled = false } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
