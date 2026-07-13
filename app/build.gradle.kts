@file:Suppress("UnstableApiUsage")

plugins {
    id("axe.android.application")
    id("axe.android.application.compose")
    id("axe.android.hilt")
}

android {
    namespace = "com.my.axe"

    defaultConfig {
        applicationId = "com.my.axe"
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "AXE_APP_SIGNATURE", "\"${System.getenv("AXE_APP_SIGNATURE") ?: "DEVELOPMENT_SIGNATURE"}\"")
        buildConfigField("boolean", "IS_BETA", "false")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
        }
        beta {
            initWith(release)
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-pre"
            buildConfigField("boolean", "IS_BETA", "true")
        }
    }

    packagingOptions.resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")

    // Disables dependency metadata when building APKs.
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        // This is for the signed .apk that we post to GitHub releases
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        // This is for the Google Play Store if we ever decide to publish there
        includeInBundle = true
    }
}
dependencies {
    implementation (projects.data)
    implementation (projects.domain)
    implementation (projects.theme)
    implementation (projects.featureStartup)
    implementation (projects.featureCrashHandler)
    implementation (projects.featureProfile)
    implementation (projects.featureAbout)
    implementation (projects.featureSettings)
    implementation (projects.featureLogs)
    implementation (projects.featureRpcBase)
    implementation (projects.featureAppsRpc)
    implementation (projects.featureMediaRpc)
    implementation (projects.featureConsoleRpc)
    implementation (projects.featureCustomRpc)
    implementation (projects.featureExperimentalRpc)
    implementation (projects.featureHome)
    implementation (projects.featureOverlay)
    implementation (projects.common.preference)
    implementation (projects.common.navigation)
    implementation (projects.gateway)

    // Extras
    implementation (libs.coil)
    implementation (libs.coil.gif)
    implementation (libs.coil.svg)
    implementation (libs.app.compat)
    implementation (libs.accompanist.navigation.animation)
    implementation (libs.kotlinx.serialization.json)


    // Material
    implementation (libs.material3)
    implementation (libs.androidx.material)
    implementation (libs.material3.windows.size)
}