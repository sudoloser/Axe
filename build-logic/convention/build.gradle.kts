import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplicationCompose") {
            id = "axe.android.application.compose"
            implementationClass = "AndroidApplicationComposePlugin"
        }
        register("androidApplication") {
            id = "axe.android.application"
            implementationClass = "AndroidApplicationPlugin"
        }
        register("androidLibraryCompose") {
            id = "axe.android.library.compose"
            implementationClass = "AndroidLibraryComposePlugin"
        }
        register("androidLibrary") {
            id = "axe.android.library"
            implementationClass = "AndroidLibraryPlugin"
        }
        register("androidFeature") {
            id = "axe.android.feature"
            implementationClass = "AndroidFeaturePlugin"
        }
        register("androidHilt") {
            id = "axe.android.hilt"
            implementationClass = "AndroidHiltPlugin"
        }
    }
}