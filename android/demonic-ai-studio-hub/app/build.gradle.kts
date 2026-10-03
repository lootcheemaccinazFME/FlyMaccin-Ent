plugins {
    id("com.android.application")
}

val fmeKeystorePath = System.getenv("FME_KEYSTORE_PATH")?.trim().orEmpty()
val fmeKeystorePassword = System.getenv("FME_KEYSTORE_PASSWORD")?.trim().orEmpty()
val fmeKeyAlias = System.getenv("FME_KEY_ALIAS")?.trim().orEmpty()
val fmeKeyPassword = System.getenv("FME_KEY_PASSWORD")?.trim().orEmpty()
val hasReleaseSigner = listOf(fmeKeystorePath, fmeKeystorePassword, fmeKeyAlias, fmeKeyPassword).all { it.isNotBlank() }

android {
    namespace = "com.flymaccin.demonicaistudio"
    compileSdk = 35

    defaultConfig {
        // Preserve the Pocket Potna / Demonic AI Studio Hut install and data lineage.
        applicationId = "com.flymaccin.pocketpotna"
        minSdk = 23
        targetSdk = 35
        versionCode = 231
        versionName = "2.1.21-unified"
    }

    signingConfigs {
        create("fmeRelease") {
            if (hasReleaseSigner) {
                storeFile = file(fmeKeystorePath)
                storePassword = fmeKeystorePassword
                keyAlias = fmeKeyAlias
                keyPassword = fmeKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = false
            if (hasReleaseSigner) {
                signingConfig = signingConfigs.getByName("fmeRelease")
            }
        }
    }
}

// UMD-030 fail-closed release gate: a release task may never fall back to
// unsigned/debug signing when the private FME release credentials are absent.
gradle.taskGraph.whenReady {
    val releaseRequested = allTasks.any { it.name.contains("release", ignoreCase = true) }
    if (releaseRequested && !hasReleaseSigner) {
        throw GradleException(
            "FME release signing is BLOCKED. Required environment variables: " +
                "FME_KEYSTORE_PATH, FME_KEYSTORE_PASSWORD, FME_KEY_ALIAS, FME_KEY_PASSWORD."
        )
    }
}
