plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.feature")
    id ("axe.android.hilt")
}

android {
    namespace = "com.my.axe.feature_profile"
}

dependencies {
    implementation (projects.theme)
    implementation (projects.gateway)
    implementation (libs.coil)
    implementation (libs.activity.compose)
    implementation (libs.kotlinx.serialization.json)
}