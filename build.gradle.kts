// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
}

// <root>/build.gradle.kts
allprojects {
    configurations.configureEach {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
        }
    }
}
