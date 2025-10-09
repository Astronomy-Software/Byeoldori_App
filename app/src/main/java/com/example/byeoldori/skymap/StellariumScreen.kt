package com.example.byeoldori.feature.skymap

import android.content.Context
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.nio.charset.Charset
import java.util.regex.Pattern

/**
 * ✅ Stellarium Web Engine (WebViewAssetLoader + HTML rewrite 버전)
 * - assets 폴더의 stellarium-web-engine 실행
 * - HTML 내의 상대경로(../, ./)를 정규화
 * - <v-navigation-drawer> 블록 전체 제거
 * - NanoHTTPD 필요 없음
 */
@Composable
fun StellariumScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = false
                        allowContentAccess = false
                        allowFileAccessFromFileURLs = false
                        allowUniversalAccessFromFileURLs = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode = WebSettings.LOAD_NO_CACHE
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        mediaPlaybackRequiresUserGesture = false
                    }

                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    // 🌙 JS 콘솔 로그
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                            Log.d(
                                "StellariumWeb",
                                "[JS] ${consoleMessage.message()} @${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}"
                            )
                            return true
                        }
                    }

                    // ✅ 향상된 WebViewClient (상대경로 정규화 + HTML 태그 제거)
                    webViewClient = StellariumAssetWebViewClientEnhanced(ctx) { url ->
                        isLoaded = true
                        Log.d("StellariumWeb", "✅ Page loaded: $url")
                    }

                    // 🌌 초기 HTML 로드https://stellarium-web.org/
//                    loadUrl("https://appassets.androidplatform.net/assets/stellarium-web-engine/apps/simple-html/stellarium-web-engine.html")
                    loadUrl("https://stellarium-web.org/")
                }
            }
        )

        // ⏳ 로딩 표시
        if (!isLoaded) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

/**
 * ✅ WebViewClient 확장 버전
 * - ../ 경로 정규화
 * - <v-navigation-drawer> 블록 제거
 */
class StellariumAssetWebViewClientEnhanced(
    private val context: Context,
    private val onLoaded: (String?) -> Unit
) : WebViewClient() {

    private val TAG = "StellariumWeb"

    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()

    // --- 경로 정규화 ---
    private fun normalizePath(path: String): String {
        val segments = mutableListOf<String>()
        for (seg in path.split("/")) {
            if (seg.isEmpty()) continue
            when (seg) {
                "." -> {}
                ".." -> if (segments.isNotEmpty()) segments.removeAt(segments.size - 1)
                else -> segments.add(seg)
            }
        }
        return "/" + segments.joinToString("/")
    }

    private fun normalizeUrlPreserveBase(urlStr: String): String {
        return try {
            val uri = URI(urlStr)
            val normPath = normalizePath(uri.path ?: "")
            URI(uri.scheme, uri.authority, normPath, uri.query, uri.fragment).toString()
        } catch (e: Exception) {
            urlStr
        }
    }

    private fun rewriteRelativePaths(html: String, baseUrl: String): String {
        val attrPattern =
            Pattern.compile("""(src|href)\s*=\s*(['"])(.+?)\2""", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matcher = attrPattern.matcher(html)
        val sb = StringBuffer()
        while (matcher.find()) {
            val attr = matcher.group(1) ?: "src"
            val quote = matcher.group(2) ?: "\""
            val raw = matcher.group(3) ?: ""
            val resolved = try {
                if (raw.startsWith("http://") || raw.startsWith("https://") ||
                    raw.startsWith("data:") || raw.startsWith("blob:")
                ) raw
                else {
                    val baseUri = URI(baseUrl)
                    val resolvedUri = baseUri.resolve(raw)
                    normalizeUrlPreserveBase(resolvedUri.toString())
                }
            } catch (e: Exception) {
                raw
            }
            val replacement = "$attr=$quote$resolved$quote"
            matcher.appendReplacement(sb, replacement.replace("\\", "\\\\").replace("$", "\\$"))
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    // --- 태그 제거 (<v-navigation-drawer>...) ---
    private fun removeNavigationDrawerBlock(html: String): String {
        val pattern = Pattern.compile(
            """<v-navigation-drawer\b[\s\S]*?>[\s\S]*?</v-navigation-drawer>""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(html)
        return matcher.replaceAll("") // 모두 제거
    }

    // --- 인터셉트 로직 ---
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url ?: return null
        val originalResponse = assetLoader.shouldInterceptRequest(url) ?: return null

        val mime = originalResponse.mimeType ?: ""
        val lowerMime = mime.lowercase()

        // HTML인 경우만 수정
        if (lowerMime.contains("text/html") || lowerMime.contains("text/")) {
            try {
                val inputStream = originalResponse.data ?: return originalResponse
                val charsetName = originalResponse.encoding ?: "utf-8"
                val bytes = inputStream.readBytes()
                val html = bytes.toString(Charset.forName(charsetName))

                var modified = removeNavigationDrawerBlock(html)
                modified = rewriteRelativePaths(modified, url.toString())

                if (modified.contains("..")) {
                    Log.d(TAG, "⚠️ 일부 상대경로가 남아있습니다: $url")
                }

                val outStream: InputStream =
                    ByteArrayInputStream(modified.toByteArray(Charset.forName(charsetName)))
                return WebResourceResponse(mime, charsetName, outStream)
            } catch (e: Exception) {
                Log.e(TAG, "❌ HTML 처리 실패: ${url} (${e.message})")
                return originalResponse
            }
        }

        return originalResponse
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onLoaded(url)
    }
}
