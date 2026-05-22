import com.android.build.api.dsl.LibraryDefaultConfig

plugins {
    id("axe.android.library")
    id("axe.android.hilt")
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.my.axe.data"
    defaultConfig {
        buildConfigFieldFromGradleProperty("BASE_URL","BASE_URL")
        buildConfigFieldFromGradleProperty("DISCORD_API_BASE_URL","DISCORD_API_URL")
        buildConfigFieldFromGradleProperty("GITHUB_API_BASE_URL","GITHUB_API_URL")
        buildConfigFieldFromGradleProperty("IMGUR_API_BASE_URL", "IMGUR_API_URL")
        buildConfigField("String", "AXE_APP_SIGNATURE", "\"${System.getenv("AXE_APP_SIGNATURE") ?: "DEVELOPMENT_SIGNATURE"}\"")
    }
}

dependencies {
    implementation (libs.core.ktx)
    implementation (projects.domain)
    implementation (libs.bundles.network.ktor)
    implementation (libs.ktor.content.negotiation)
    implementation (libs.ktor.logging)
    implementation (projects.common.preference)
    implementation (projects.common.resources)
    api (projects.gateway)
    implementation (libs.blankj.utilcodex)
    testImplementation(libs.junit)
}

fun LibraryDefaultConfig.buildConfigFieldFromGradleProperty(fieldName: String, gradlePropertyName: String) {
    val propertyValue = project.properties[gradlePropertyName] as? String
    if (propertyValue != null) {
        buildConfigField("String", fieldName, propertyValue)
    }
}