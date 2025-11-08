package com.example.byeoldori.skymap

import AppBridge
import android.app.Activity
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun StellariumScreen(mode: SkyMode) {
    val context = LocalContext.current
    val viewModel: ObjectDetailViewModel = hiltViewModel()
    val window = (context as Activity).window

    // ✅ 상태바 숨김
    LaunchedEffect(Unit) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    DisposableEffect(Unit) {
        onDispose {
            WindowInsetsControllerCompat(window, window.decorView)
                .show(WindowInsetsCompat.Type.statusBars())
        }
    }

    val webViewState = remember { mutableStateOf<WebView?>(null) }
    val skyCameraController = remember { SkyCameraController(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                createStellariumWebView(ctx, viewModel, skyCameraController).also { webView ->
                    webViewState.value = webView

                    // ✅ 싱글톤 컨트롤러에 WebView 등록
                    StellariumController.bindWebView(webView)
                    println("✅ StellariumController WebView 바인딩 완료")
                }
            }
        )

        ObjectDetailScreen(viewModel)
    }

    // ✅ SWE 엔진과 WebView 모두 준비된 후 동작
    LaunchedEffect(
        viewModel.sweEngineReady.collectAsState().value && (webViewState.value != null)
    ) {
        viewModel.resetSweEngineReady()

        // 싱글톤 컨트롤러를 그대로 사용
        skyCameraController.bindToStellarium(StellariumController)

        when (mode) {
            SkyMode.EDUCATION -> StellariumController.setEducationMode()
            SkyMode.OBSERVATION -> StellariumController.toggleConstellations(false)
        }

        println("✅ SWE 엔진 & WebView 모두 준비 완료 — Controller 바인딩 완료")
    }

    // ✅ 화면 해제 시 메모리 누수 방지
    DisposableEffect(Unit) {
        onDispose {
            skyCameraController.stop()
            StellariumController.clearBinding()
            println("🧹 StellariumController WebView 연결 해제 완료")
        }
    }
}

private fun createStellariumWebView(
    context: Context,
    viewModel: ObjectDetailViewModel,
    skyCameraController: SkyCameraController
): WebView {
    return WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true

        addJavascriptInterface(AppBridge(context, skyCameraController, viewModel), "AndroidBridge")
        webViewClient = WebViewClient()
        loadUrl("http://localhost:8080/")
    }
}

enum class SkyMode {
    OBSERVATION,  // 일반 관측 모드
    EDUCATION     // 교육 모드 (별도리 학습용)
}
