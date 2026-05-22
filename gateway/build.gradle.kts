plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    api (libs.okhttp)
    implementation (libs.okhttp.logging)
    implementation (projects.domain)
    implementation (libs.kotlinx.coroutine)
    implementation (libs.bundles.network.ktor)
    implementation (libs.ktor.websockets)
    testImplementation (libs.junit)
}