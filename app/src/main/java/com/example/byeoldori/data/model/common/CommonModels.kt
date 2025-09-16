package com.example.byeoldori.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

@JsonClass(generateAdapter = true)
data class TokenData(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "refreshToken") val refreshToken: String,
    @Json(name = "accessTokenExpiresAt") val accessTokenExpiresAt: Long,
    @Json(name = "refreshTokenExpiresAt") val refreshTokenExpiresAt: Long
) {
    /**
     * 서버가 epochSecond로 내려줄 경우에 대비해서 millis 변환 제공
     * (서버 스펙이 확실히 millis라면 필요 없음)
     */
    fun accessTokenExpiresAtMillis(): Long {
        return if (accessTokenExpiresAt < 2_000_000_000L) accessTokenExpiresAt * 1000
        else accessTokenExpiresAt
    }

    fun refreshTokenExpiresAtMillis(): Long {
        return if (refreshTokenExpiresAt < 2_000_000_000L) refreshTokenExpiresAt * 1000
        else refreshTokenExpiresAt
    }
}
