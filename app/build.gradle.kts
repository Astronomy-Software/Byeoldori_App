import java.io.FileInputStream
        import java.util.Properties

        plugins {
            alias(libs.plugins.android.application)
            alias(libs.plugins.kotlin.android)
            alias(libs.plugins.hilt)
            alias(libs.plugins.ksp)
            alias(libs.plugins.firebase.appdistribution)
        }

val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProperties.load(FileInputStream(localFile))
}

fun prop(key: String, default: String? = null): String {
    val v = localProperties.getProperty(key) ?: default
    return requireNotNull(v) { "Missing '$key' in local.properties" }
}

fun withTrailingSlash(url: String) =
    if (url.endsWith("/")) url else "$url/"

android {
    namespace = "com.example.byeoldori"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.byeoldori"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "NAVER_CLIENT_ID", "\"${localProperties.getProperty("NAVER_CLIENT_ID")}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${localProperties.getProperty("NAVER_CLIENT_SECRET")}\"")

        // ✅ Hilt 테스트 러너를 실제로 쓸 때만 유지. 아니면 아래 한 줄로 바꾸세요.
        testInstrumentationRunner = "com.example.byeoldori.HiltTestRunner"
        // 일반 러너 예시: testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions { jvmTarget = "21" }

    buildTypes {
        debug {
            val base = withTrailingSlash(prop("BASE_URL_DEBUG", "http://10.0.2.2:8080/"))
            buildConfigField("String", "BASE_URL", "\"$base\"")
        }
        release {
            val base = withTrailingSlash(prop("BASE_URL_RELEASE", "https://api.example.com/"))
            buildConfigField("String", "BASE_URL", "\"$base\"")
            isMinifyEnabled = true
        }
    }

    kotlin { jvmToolchain(21) }

}

dependencies {
    implementation(project(":live2d:Framework:framework"))
    implementation(project(":live2d:live2dview"))
//    implementation(fileTree(dir = "live2d/Core/android", include = ["Live2DCubismCore.aar"]))
//    implementation(project(":live2d:Core"))
//    implementation(project(":live2d:Sample"))
    implementation(platform(libs.compose.bom))
    implementation(libs.coil.compose)
    implementation(libs.activity.compose)
    implementation(libs.ui)
    implementation(libs.material3)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
//    implementation(libs.moshi.kotlin)
    // 네이버 맵 - 공식, compose
    implementation(libs.map.sdk)
    implementation(libs.naver.map.compose)
    implementation(libs.play.services.location)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)


    // ✅ Hilt 본체
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(project(":live2d:Framework:framework"))

    // ✅ Moshi는 계속 KSP 사용
    ksp(libs.moshi.kotlin.codegen)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.ui.tooling)

    // 테스트
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ui.test.junit4)
    
    // ✅ Hilt 계측 테스트를 실제로 쓰면 아래 2줄 유지
    androidTestImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    kspAndroidTest(libs.hilt.compiler)

    debugImplementation(libs.ui.tooling)
}

hilt {
    enableAggregatingTask = false
}