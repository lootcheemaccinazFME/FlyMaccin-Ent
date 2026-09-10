plugins { id("com.android.application") }

android {
    namespace = "com.flymaccin.bookwriter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.flymaccin.bookwriter.openai"
        minSdk = 26
        targetSdk = 35
        versionCode = 122
        versionName = "1.2.2-house-core-loop"
    }

    signingConfigs {
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
            isMinifyEnabled = false
            isShrinkResources = false
            if (!System.getenv("FME_KEYSTORE_PATH").isNullOrBlank()) signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
