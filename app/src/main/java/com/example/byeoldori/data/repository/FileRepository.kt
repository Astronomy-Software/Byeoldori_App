package com.example.byeoldori.data.repository

import android.util.Log
import com.example.byeoldori.data.api.FileUploadApi
import com.example.byeoldori.data.api.ProgressRequestBody
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject

private const val TAG = "FileRepository"
private const val MAX_IMAGE_BYTES = 10L * 1024 * 1024 // 10MB

class FileRepository @Inject constructor(
    private val api: FileUploadApi
) {
    suspend fun uploadImage(
        file: File,
        token: String? = null,
        onProgress: ((sent: Long,total: Long) -> Unit)? = null
    ): String? {
        val size = file.length()
        if (size > MAX_IMAGE_BYTES) {
            throw IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.")
        }
        val contentType = "image/jpeg"
        val body = ProgressRequestBody(file,contentType) { sent, total ->
            onProgress?.invoke(sent,total)
        }
        val part = MultipartBody.Part.createFormData("file",file.name,body)
        val authHeader = token?.let { "Bearer $it" }

        return try {
            val response = api.uploadImage(part, authHeader)
            if (response.success) {
                response.data.url
            } else {
                Log.e(TAG, "업로드 실패: ${response.message}")
                throw IllegalStateException(response.message ?: "업로드 실패")
            }
        } catch (e: Exception) {
            Log.e(TAG, "이미지 업로드 중 오류: ${e.message}", e)
            throw e
        }
    }
}