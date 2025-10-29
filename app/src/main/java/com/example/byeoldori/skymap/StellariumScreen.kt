package com.example.byeoldori.skymap

import android.app.Activity
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
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.launch

@Composable
fun StellariumScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ 상태바 컨트롤러 준비
    val window = (context as Activity).window
    val insetsController = remember {
        WindowInsetsControllerCompat(window, window.decorView)
    }
    // ✅ 진입 시 상태바 숨기기 & 나갈 때 복원
    DisposableEffect(Unit) {
        // 상단 상태바만 숨김
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            // 상태바 다시 보이게
            insetsController.show(WindowInsetsCompat.Type.statusBars())
        }
    }

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
                // 브릿지 등록
                addJavascriptInterface(AppBridge(context,gyroController), "AndroidBridge")
                webViewClient = WebViewClient()
                loadUrl("http://localhost:8080/")
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
//                kotlinx.coroutines.delay(10000L)  // 10초 지연
//                controller.setLocation(37.5665, 126.9780, 38.0)
//                // ✅ 현재 시각 ISO 8601 UTC 포맷
//                val nowIso = DateTimeFormatter.ISO_INSTANT
//                    .withZone(ZoneOffset.UTC)
//                    .format(Instant.now())
//                controller.setTime(nowIso)   // 여기서 바로 전달 👈
            }

            onDispose {
                job.cancel()
                gyroController.stop()
            }
        } else onDispose { }
    }
}