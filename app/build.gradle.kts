plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.modutrap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.modutrap"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables { useSupportLibrary = true }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing. A keystore is only used when the file exists AND the
    // required env vars are provided (by the CI release workflow). This keeps
    // plain `./gradlew assembleDebug` and `assembleRelease` working with zero
    // configuration: without a keystore the release build falls back to the
    // debug signing config, producing an installable (debug-signed) APK.
    //
    // Env vars consumed from CI secrets:
    //   KS_PASS     — keystore store password
    //   KS_KEY_PASS — key password (falls back to KS_PASS when unset)
    //   KS_ALIAS    — key alias in the keystore (defaults to "modutrap")
    signingConfigs {
        create("release") {
            val ksFile = rootProject.file("app/release.keystore")
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = System.getenv("KS_PASS") ?: "changeit"
                keyAlias = System.getenv("KS_ALIAS") ?: "modutrap"
                keyPassword = System.getenv("KS_KEY_PASS") ?: System.getenv("KS_PASS") ?: "changeit"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val ksFile = rootProject.file("app/release.keystore")
            signingConfig = if (ksFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            isMinifyEnabled = false
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        unitTests { isReturnDefaultValues = true }
    }

    // D8/R8 dexer heap. The dexer runs in a worker JVM that defaults to a small
    // heap (~512m), which can OOM while merging external dex archives (Shizuku
    // + AndroidX + Compose + Coroutines pull in a lot of classes). 2048m keeps
    // assembleDebug reliable on CI runners.
    @Suppress("DEPRECATION")
    dexOptions {
        javaMaxHeapSize = "2048m"
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.service)
    implementation(libs.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.datastore.preferences)

    // Shizuku — optional privileged integration (writes ClientAppSettings.json
    // into Roblox's data directory and runs diagnostic shell commands).
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.shizuku.aidl)
    implementation(libs.shizuku.shared)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
}
