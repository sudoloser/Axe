plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.feature")
}

android {
    namespace = "com.my.axe.feature_home"
    defaultConfig {
        buildConfigField("String","VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
    }
}

dependencies {
    implementation (projects.featureOverlay)
    implementation (libs.accompanist.flowLayout)
    implementation (libs.material.icons.extended)
    implementation (projects.featureRpcBase)
    implementation (projects.featureSettings)
    implementation (projects.common.navigation)
    implementation (libs.coil)
    implementation (libs.activity.compose)
    implementation (libs.kotlinx.serialization.json)
}