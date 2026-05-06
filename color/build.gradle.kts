
plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
}
android {
    namespace = "com.axe.color"
}
dependencies {
    api(libs.compose.ui)
    api(libs.core.ktx)
    api(libs.material3)
}