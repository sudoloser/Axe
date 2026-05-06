plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.feature")
}

android {
    namespace = "com.my.axe.feature_crash_handler"
}

dependencies {
    implementation (libs.activity.compose)
    implementation(libs.crashx)
    implementation (libs.blankj.utilcodex)
    implementation (libs.material.icons.extended)
    implementation (projects.theme)
}