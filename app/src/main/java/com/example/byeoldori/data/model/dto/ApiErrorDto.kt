package com.example.byeoldori.data.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 서버 에러 바디를 파싱하기 위한 범용 DTO
 * 예) { "message":"Invalid", "errors": { "email":["..."] }, "code":123, "status":400 }
 */
@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    val code: Int? = null,            // 서비스 내부 에러 코드
    val status: Int? = null,          // HTTP status를 복제해서 주는 서버도 있음
    val message: String? = null,      // 대표 메시지
    @Json(name = "error") val error: String? = null, // 어떤 서버는 message 대신 error 키 사용
    val errors: Map<String, List<String>>? = null    // 필드별 상세 오류
) {
    fun readableMessage(): String =
        message ?: error
        ?: errors?.entries?.joinToString { (k, v) -> "$k: ${v.joinToString()}" }
        ?: "알 수 없는 오류"
}
