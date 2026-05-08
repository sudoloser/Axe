plugins {
    id ("axe.android.library")
    id ("axe.android.feature")
    id ("axe.android.hilt")
}

android {
    namespace = "com.my.axe.feature_rpc_base"
}

dependencies {
    implementation (libs.shizuku.api)
    implementation (libs.blankj.utilcodex)
    implementation(libs.androidx.material)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil)
}