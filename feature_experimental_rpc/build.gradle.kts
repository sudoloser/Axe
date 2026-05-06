plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.hilt")
    id ("axe.android.feature")
}

android {
    namespace = "com.my.axe.feature_experimental_rpc"
}

dependencies {
    implementation(libs.material.icons.extended)
    implementation(libs.activity.compose)
    implementation(libs.blankj.utilcodex)
    implementation(libs.coil)
    implementation(projects.featureRpcBase)
}
