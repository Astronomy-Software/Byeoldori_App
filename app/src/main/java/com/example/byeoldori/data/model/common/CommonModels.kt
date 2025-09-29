package com.example.byeoldori.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

@JsonClass(generateAdapter = true)
data class TokenData(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "refreshToken") val refreshToken: String,
    @Json(name = "accessTokenExpiresAt") val accessTokenExpiresAt: String,
    @Json(name = "refreshTokenExpiresAt") val refreshTokenExpiresAt: String
) {
    /**
     * ISO-8601 문자열 또는 epoch(long) 모두 처리 가능
     */
    fun accessTokenExpiresAtMillis(): Long {
        return parseToMillis(accessTokenExpiresAt)
    }

    fun refreshTokenExpiresAtMillis(): Long {
        return parseToMillis(refreshTokenExpiresAt)
    }

    private fun parseToMillis(value: String): Long {
        return try {
            // 먼저 Long으로 파싱 (서버가 epoch 내려주는 경우)
            val asLong = value.toLong()
            if (asLong < 2_000_000_000L) asLong * 1000 else asLong
        } catch (_: NumberFormatException) {
            // Long으로 안 되면 ISO8601 포맷으로 파싱
            java.time.OffsetDateTime.parse(value)
                .toInstant()
                .toEpochMilli()
        }
    }
}