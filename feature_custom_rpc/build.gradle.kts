plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.feature")
    id ("axe.android.hilt")
}

android {
    namespace = "com.my.axe.feature_custom_rpc"
}

dependencies {
    implementation (projects.data)
    implementation (libs.material.icons.extended)
    implementation(libs.accompanist.permission)
    implementation(libs.activity.compose)
    implementation(libs.blankj.utilcodex)
    implementation(libs.coil)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.featureRpcBase)
    implementation(projects.featureProfile)
}