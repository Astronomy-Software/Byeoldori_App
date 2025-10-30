package com.example.byeoldori.skymap

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class StellariumServer private constructor(
    private val appContext: Context,
    port: Int = 8080
) : NanoHTTPD(port) {

    companion object {
        @Volatile
        private var instance: StellariumServer? = null

        /** 서버 인스턴스를 생성하고, 이미 있으면 재사용 */
        fun startIfNeeded(context: Context): StellariumServer {
            synchronized(this) {
                if (instance == null) {
                    instance = StellariumServer(context.applicationContext).apply { start() }
                }
                return instance!!
            }
        }

        /** 서버 정지 */
        fun stopServer() {
            synchronized(this) {
                instance?.stop()
                instance = null
            }
        }

        /** 서버 실행 여부 확인 */
        fun isRunning(): Boolean = instance != null
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri

        // ✅ proxy 엔드포인트
        if (uri.startsWith("/proxy")) {
            val params = session.parameters["url"]
            if (params.isNullOrEmpty()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing url")
            }
            val targetUrl = params[0]
            return proxyRequest(targetUrl)
        }

        // ✅ 정적 파일 서빙
        val cleanUri = uri.trimStart('/')
        val assetPath = if (cleanUri.isEmpty() || cleanUri == "/") {
            "StellariumServer/index.html"
        } else {
            "StellariumServer/$cleanUri"
        }

        return try {
            val inputStream: InputStream = appContext.assets.open(assetPath)
            val mimeType = getMimeType(assetPath)
            newChunkedResponse(Response.Status.OK, mimeType, inputStream)
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found: $assetPath")
        }
    }

    private fun proxyRequest(targetUrl: String): Response {
        return try {
            val url = URL(targetUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val contentType = connection.contentType ?: "application/json"
            val responseStream = connection.inputStream
            val data = responseStream.bufferedReader().use { it.readText() }

            val resp = newFixedLengthResponse(Response.Status.OK, contentType, data)
            resp.addHeader("Access-Control-Allow-Origin", "*")
            resp.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            resp.addHeader("Access-Control-Allow-Headers", "Content-Type")
            resp
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Proxy error: ${e.message}")
        }
    }

    private fun getMimeType(path: String): String = when {
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