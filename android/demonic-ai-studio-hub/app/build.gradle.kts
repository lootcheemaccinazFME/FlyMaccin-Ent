plugins {
    id("com.android.application")
}

android {
    namespace = "com.flymaccin.demonicaistudio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.flymaccin.demonicaistudio"
        minSdk = 23
        targetSdk = 35
        versionCode = 110
        versionName = "1.1.0-studio"
    }

    signingConfigs {
        create("studioDebug") {
            storeFile = file("studio-debug.keystore")
            storePassword = "demonicdebug"
            keyAlias = "demonicdebug"
            keyPassword = "demonicdebug"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("studioDebug")
        }
    }
}
