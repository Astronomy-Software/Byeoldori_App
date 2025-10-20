package com.example.byeoldori.data.repository

import android.util.Log
import com.example.byeoldori.data.api.FileUploadApi
import com.example.byeoldori.data.model.dto.FileUploadResponse
import com.example.byeoldori.data.remote.MultipartFactory
import java.io.File
import javax.inject.Inject

class FileRepository @Inject constructor(
    private val api: FileUploadApi
) {
    suspend fun uploadImage(file: File, token: String? = null): String? {
        return try {
            val part = MultipartFactory.imagePart(file) //파일을 MultipartBody.Part로 변환하는 헬퍼 클래스
            val response: FileUploadResponse = api.uploadImage(part,token?.let { "Bearer $it" }) //만약 토큰이 있다면 Bearer<token>형태로 헤더에 포함
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