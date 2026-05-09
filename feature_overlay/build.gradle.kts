plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.feature")
    id ("axe.android.hilt")
}

android {
    namespace = "com.my.axe.feature_overlay"
}

dependencies {
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.common.preference)
    implementation(projects.common.resources)
    implementation(projects.featureRpcBase)
    implementation(projects.theme)
    implementation(projects.color)

    implementation(libs.androidx.material)
    implementation(libs.material.icons.extended)
    implementation(libs.coil)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.blankj.utilcodex)
}
