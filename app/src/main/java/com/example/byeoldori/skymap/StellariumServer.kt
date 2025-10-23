package com.example.byeoldori.skymap

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.InputStream

class StellariumServer(
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
