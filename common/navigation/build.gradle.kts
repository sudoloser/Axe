plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
}

android {
    namespace = "com.my.axe.navigation"
}

dependencies {
    implementation (libs.compose.ui)
    implementation (libs.compose.navigation)
    implementation (libs.accompanist.navigation.animation)
}