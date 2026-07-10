plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.feature")
}

android {
    namespace = "com.my.axe.feature_settings"

    defaultConfig {
        buildConfigField("String","VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
    }
}

dependencies {
    implementation (libs.blankj.utilcodex)
    implementation(libs.androidx.material)
    implementation(libs.material.icons.extended)
    implementation(libs.accompanist.pager.layouts)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.coil)
    implementation(libs.coil.svg)
    implementation(libs.android.svg)
    implementation(libs.kotlinx.serialization.json)

    implementation(projects.color)
    implementation(projects.theme)
}