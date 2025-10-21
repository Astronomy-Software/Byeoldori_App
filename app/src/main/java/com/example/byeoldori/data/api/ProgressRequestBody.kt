package com.example.byeoldori.data.api

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val onProgress: (sent: Long,total: Long) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()
    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val total = contentLength()
        file.source().use { src ->
            var sent = 0L
            val buffer = okio.Buffer()
            var read: Long
            while(src.read(buffer,8_192).also { read = it} != -1L) {
                sink.write(buffer,read)
                sent += read
                onProgress(sent,total)
            }
        }
    }
}