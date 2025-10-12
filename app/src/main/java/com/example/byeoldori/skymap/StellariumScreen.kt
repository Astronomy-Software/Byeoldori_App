package com.example.byeoldori.skymap

import android.content.Context
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
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.launch
import java.io.InputStream

/**
 * ✅ 완전 통합 단일 파일 버전
 * - /assets/StellariumServer/ → http://localhost:14204/ 로 매핑
 * - HTML, CSS, JS, 이미지 전부 로컬에서 서빙
 * - JS 라우팅, fetch(), router-link 등 완전 지원
 */

@Composable
fun StellariumScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1) 로컬 서버 시작/정지
    val server = remember {
        LocalWebServer(context).apply { start() }
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
                kotlinx.coroutines.delay(10000L)  // 3초 지연
                controller.setViewDirection(180.0, 45.0)
                controller.setLocation(37.5665, 126.9780, 38.0)
                gyroController.start()
            }

            onDispose {
                job.cancel()
                gyroController.stop()
            }
        } else onDispose { }
    }
}

/**
 * NanoHTTPD 기반 로컬 서버
 * assets/StellariumServer 폴더를 http://localhost:14204/ 로 서빙
 */
class LocalWebServer(
    private val appContext: Context,
    port: Int = 14204
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri.trimStart('/')
        val assetPath = if (uri.isEmpty() || uri == "/") {
            "StellariumServer/index.html"
        } else {
            "StellariumServer/$uri"
        }

        return try {
            val inputStream: InputStream = appContext.assets.open(assetPath)
            val mimeType = getMimeType(assetPath)
            newChunkedResponse(Response.Status.OK, mimeType, inputStream)
        } catch (e: Exception) {
            newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "text/plain",
                "404 Not Found: $assetPath"
            )
        }
    }

    private fun getMimeType(path: String): String {
        return when {
            path.endsWith(".html") -> "text/html"
            path.endsWith(".js") -> "application/javascript"
            path.endsWith(".css") -> "text/css"
            path.endsWith(".png") -> "image/png"
            path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
            path.endsWith(".ico") -> "image/x-icon"
            path.endsWith(".json") -> "application/json"
            path.endsWith(".svg") -> "image/svg+xml"
            else -> "text/plain"
        }
    }
}
