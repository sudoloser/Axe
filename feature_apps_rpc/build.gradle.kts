plugins {
    id ("axe.android.library")
    id ("axe.android.library.compose")
    id ("axe.android.feature")
}

android {
    namespace = "com.my.axe.feature_apps_rpc"
}

dependencies {
    implementation (projects.data)
    implementation(projects.featureRpcBase)
    implementation(projects.featureCustomRpc)
    implementation (libs.material.icons.extended)
}