import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProperties.load(FileInputStream(localFile))
}


android {
    namespace = "com.example.byeoldori"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.byeoldori"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "NAVER_CLIENT_ID", "\"${localProperties.getProperty("NAVER_CLIENT_ID")}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${localProperties.getProperty("NAVER_CLIENT_SECRET")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform(libs.compose.bom))

    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("com.google.android.material:material:1.11.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // 네이버 맵 - 공식
    implementation("com.naver.maps:map-sdk:3.21.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Naver Map Compose
    implementation("io.github.fornewid:naver-map-compose:1.5.0")
    implementation("com.google.accompanist:accompanist-permissions:0.33.2-alpha")

}
