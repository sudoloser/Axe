plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.feature")
    id ("axe.android.hilt")
}

android {
    namespace = "com.my.axe.feature_about"

    defaultConfig {
        buildConfigField("String","VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
    }
}

dependencies {
    implementation (libs.coil)
    implementation (libs.material.icons.extended)
    implementation (libs.activity.compose)
}
