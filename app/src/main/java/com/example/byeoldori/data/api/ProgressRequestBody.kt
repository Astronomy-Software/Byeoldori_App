package com.example.byeoldori.data.api

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File

class ProgressRequestBody( //이미지 업로드 진행률을 추적하기 위함
    private val file: File,
    private val contentType: String,
    private val onProgress: (sent: Long,total: Long) -> Unit //지금까지 보낸 바이트 수
) : RequestBody() {

    override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()
    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val total = contentLength()
        file.source().use { src ->
            var sent = 0L
            val buffer = okio.Buffer()
            var read: Long
            while(src.read(buffer,8_192).also { read = it} != -1L) { //파일을 8KB단위로 읽음
                sink.write(buffer,read)
                sent += read
                onProgress(sent,total)
            }
        }
    }
}