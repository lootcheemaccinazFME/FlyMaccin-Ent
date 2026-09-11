plugins {
    id("com.android.application")
}

android {
    namespace = "com.flymaccin.demonicdaw"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.flymaccin.demonicdaw"
        minSdk = 26
        targetSdk = 35
        versionCode = 100
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
