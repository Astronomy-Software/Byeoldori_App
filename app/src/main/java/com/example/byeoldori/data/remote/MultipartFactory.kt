package com.example.byeoldori.data.remote

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 File : 오리온자리.jpg (핸드폰 안에 있는 실제 사진 파일)
 Retrofit은 서버랑 통신할 때, 단순한 File 객체가 아니라 HTTP 요청 형식으로 데이터를 주고받음
 asRequestBody()를 사용해 파일 안의 실제 내용을 읽어
 네트워크로 전송 가능한 RequestBody 형태로 변환
 */

object MultipartFactory {
    fun imagePart( //File -> MultipartBody.Part로 바꿔주는 변환기
        file: File,
        partName: String = "file",
        contentType: String = "image/jpeg" //전송할 파일의 MIME 타입
    ): MultipartBody.Part {
        val requestFile = file.asRequestBody(contentType.toMediaTypeOrNull()) //파일을 HTTP요청 본문으로 감쌈
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
}