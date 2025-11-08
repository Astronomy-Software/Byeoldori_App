package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.dto.FileUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileUploadApi {
    @Multipart //파일+텍스트를 동시에 전송할 때 사용(요청이 여러 부분으로 구성)
    @POST("files/image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Header("Authorization") authorization: String? = null //요청 헤더에 토큰 추가
    ): FileUploadResponse
}