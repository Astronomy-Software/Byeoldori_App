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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.skymap.viewmodel.SkyObjectViewModel

@Composable
fun StellariumScreen() {
    val context = LocalContext.current
    val viewModel: SkyObjectViewModel = hiltViewModel()
    val window = (context as Activity).window

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
//    val cameraTracker = remember { SkyCameraTracker() }
//    val gyroController = remember { GyroCameraController(context, cameraTracker) }
    val skyCameraController = remember { SkyCameraController(context) }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                createStellariumWebView(ctx, viewModel, skyCameraController).also {
                    webViewState.value = it
                }
            }
        )
        SkymapDetailScreen(viewModel)
    }

    // ✅ Controller 바인딩
    LaunchedEffect(webViewState.value) {
        webViewState.value?.let {
            val controller = StellariumWebController(it)
            skyCameraController.bindToStellarium(controller)
        }
    }

    DisposableEffect(Unit) {
        onDispose { skyCameraController.stop() }
    }
}

private fun createStellariumWebView(
    context: Context,
    viewModel: SkyObjectViewModel,
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
