@file:Suppress("UnstableApiUsage")

plugins {
    id("axe.android.application")
    id("axe.android.application.compose")
    id("axe.android.hilt")
}

android {
    namespace = "com.my.axe"

    defaultConfig {
        versionCode = libs.versions.version.code.get().toInt()
        versionName = libs.versions.version.name.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "AXE_APP_SIGNATURE", "\"${System.getenv("AXE_APP_SIGNATURE") ?: "DEVELOPMENT_SIGNATURE"}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions.add("distribution")
    productFlavors {
        create("user") {
            dimension = "distribution"
            applicationId = "com.my.axe"
            manifestPlaceholders["persistent"] = "false"
            manifestPlaceholders["gatewayProcess"] = "" // default process
        }
        create("system") {
            dimension = "distribution"
            applicationId = "com.my.axe.system"
            manifestPlaceholders["persistent"] = "true"
            manifestPlaceholders["gatewayProcess"] = ":gateway"
        }
    }

    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            val flavor = productFlavors[0].name
            val buildType = buildType.name
            if (buildType == "release") {
                outputFileName = if (flavor == "system") "axe-system-release.apk" else "axe-release.apk"
            }
        }
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
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
    implementation (libs.shizuku.provider)
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

    // Extras
    implementation (libs.app.compat)
    implementation (libs.accompanist.navigation.animation)
    implementation (libs.kotlinx.serialization.json)


    // Material
    implementation (libs.material3)
    implementation (libs.androidx.material)
    implementation (libs.material3.windows.size)
}