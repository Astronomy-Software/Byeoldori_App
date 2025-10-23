package com.example.byeoldori.skymap

import android.webkit.WebView
import android.webkit.WebViewClient
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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun StellariumScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1) 로컬 서버 시작/정지
    val server = remember {
        StellariumServer(context).apply { start() }
    }
    DisposableEffect(Unit) {
        onDispose { server.stop() }
    }

    // 2) WebView 참조 보관
    val webViewState = remember { mutableStateOf<WebView?>(null) }

    // 3) Tracker / GyroController 준비
    val cameraTracker = remember { SkyCameraTracker() }
    val gyroController = remember { GyroCameraController(context, cameraTracker) }

    // 4) WebView 표시
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                webViewClient = WebViewClient()
                loadUrl("http://localhost:14204/")
                webViewState.value = this
            }
        }
    )

    // 5) WebView가 준비되면 Controller 생성 (webViewState.value가 바뀔 때마다 갱신)
    val controller = remember(webViewState.value) {
        webViewState.value?.let { StellariumWebController(it) }
    }

    // 6) Controller 준비 후 Tracker 바인딩
    LaunchedEffect(controller) {
        controller?.let { cameraTracker.bindToStellarium(it) }
    }

    // 7) 자이로 start/stop & (옵션) 초기 명령
    DisposableEffect(controller) {
        if (controller != null) {
            val job = scope.launch {
                kotlinx.coroutines.delay(10000L)  // 10초 지연
                controller.setLocation(37.5665, 126.9780, 38.0)
                // ✅ 현재 시각 ISO 8601 UTC 포맷
                val nowIso = DateTimeFormatter.ISO_INSTANT
                    .withZone(ZoneOffset.UTC)
                    .format(Instant.now())
                controller.setTime(nowIso)   // 여기서 바로 전달 👈
                gyroController.start()
            }

            onDispose {
                job.cancel()
                gyroController.stop()
            }
        } else onDispose { }
    }
}