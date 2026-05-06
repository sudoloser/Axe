plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
}

android {
    namespace = "com.my.axe.ui.theme"
}

dependencies {
    implementation (libs.material3)
    implementation (libs.material3.windows.size)
    implementation (projects.domain)
    implementation (projects.common.preference)
    implementation (projects.color)
    implementation (libs.androidx.material)
    implementation (libs.accompanist.systemUiController)
}