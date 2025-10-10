package com.example.byeoldori.skymap

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import fi.iki.elonen.NanoHTTPD
import java.io.InputStream

/**
 * ✅ 완전 통합 단일 파일 버전
 * - /assets/StellariumServer/ → http://localhost:9999/ 로 매핑
 * - HTML, CSS, JS, 이미지 전부 로컬에서 서빙
 * - JS 라우팅, fetch(), router-link 등 완전 지원
 */

@Composable
fun StellariumScreen() {
    val context = LocalContext.current
    // NanoHTTPD 서버 생성 및 실행
    val server = remember {
        LocalWebServer(context).apply {
            start()
        }
    }

    // Compose 생명주기 종료 시 서버 종료
    DisposableEffect(Unit) {
        onDispose {
            server.stop()
        }
    }

    // WebView 표시
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                webViewClient = WebViewClient()

                // ✅ 로컬 서버 URL로 접속
                loadUrl("http://localhost:14204/")
            }
        }
    )
}

/**
 * ✅ NanoHTTPD 기반 로컬 서버
 * assets/StellariumServer 폴더를 http://localhost:9999/ 로 서빙
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
            // 404 응답
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
