plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.feature")
}

android {
    namespace = "com.my.axe.feature_startup"
}

dependencies {
    implementation (libs.activity.compose)
    implementation (libs.material.icons.extended)
    implementation (libs.accompanist.permission)
    implementation (libs.kotlinx.serialization.json)
}