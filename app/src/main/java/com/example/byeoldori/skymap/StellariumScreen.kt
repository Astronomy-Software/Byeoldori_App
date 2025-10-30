package com.example.byeoldori.skymap

import AppBridge
import android.app.Activity
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.skymap.viewmodel.SkyObjectViewModel
import kotlinx.coroutines.launch

@Composable
fun StellariumScreen() {
    val context = LocalContext.current
    val viewModel: SkyObjectViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    // ✅ 상태바 제어
    val window = (context as Activity).window
    val insetsController = remember { WindowInsetsControllerCompat(window, window.decorView) }

    DisposableEffect(Unit) {
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose { insetsController.show(WindowInsetsCompat.Type.statusBars()) }
    }

    // ✅ WebView 상태
    val webViewState = remember { mutableStateOf<WebView?>(null) }

    // ✅ 자이로 및 카메라 제어
    val cameraTracker = remember { SkyCameraTracker() }
    val gyroController = remember { GyroCameraController(context, cameraTracker) }

    // ✅ WebView + DetailScreen Overlay
    Box(modifier = Modifier.fillMaxSize()) {
        // 🌌 Stellarium Web Engine
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    addJavascriptInterface(AppBridge(context, gyroController, viewModel), "AndroidBridge")
                    webViewClient = WebViewClient()
                    loadUrl("http://localhost:8080/")
                    webViewState.value = this
                }
            }
        )

        // 🌟 천체 상세정보 패널 (AnimatedVisibility)
        SkymapDetailScreen(viewModel)
    }

    // ✅ Controller 세팅
    val controller = remember(webViewState.value) {
        webViewState.value?.let { StellariumWebController(it) }
    }

    // ✅ Tracker 바인딩
    LaunchedEffect(controller) {
        controller?.let { cameraTracker.bindToStellarium(it) }
    }

    // ✅ 종료 처리
    DisposableEffect(controller) {
        if (controller != null) {
            val job = scope.launch { }
            onDispose {
                job.cancel()
                gyroController.stop()
            }
        } else onDispose { }
    }
}
