package com.example.byeoldori.data.repository

import android.util.Log
import com.example.byeoldori.data.api.FileUploadApi
import com.example.byeoldori.data.api.ProgressRequestBody
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject

class FileRepository @Inject constructor(
    private val api: FileUploadApi
) {
    suspend fun uploadImage(
        file: File,
        token: String? = null,
        onProgress: ((sent: Long,total: Long) -> Unit)? = null
    ): String? {
        return try {
            val contentType = "image/jpeg"
            val body = ProgressRequestBody(file,contentType) { sent, total ->
                onProgress?.invoke(sent,total)
            }
            val part = MultipartBody.Part.createFormData("file",file.name,body)
            val response = api.uploadImage(part,token?.let { "Bearer $it" }) //만약 토큰이 있다면 Bearer<token>형태로 헤더에 포함

            if(response.success) {
                response.data.url //서버가 반환한 이미지 URL을 리턴
            } else {
                Log.e("FileRepository", "업로드 실패: ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "이미지 업로드 중 오류", e)
            null
        }
    }
}