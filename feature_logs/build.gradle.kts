plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.feature")
    id ("axe.android.hilt")
}

android {
    namespace = "com.my.axe.feature_logs"
}

dependencies {
    implementation (projects.theme)
    implementation(libs.activity.compose)
}