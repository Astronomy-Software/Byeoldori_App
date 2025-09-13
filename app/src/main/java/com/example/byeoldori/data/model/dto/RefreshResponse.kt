package com.example.byeoldori.data.model.dto

// src/main/java/com/example/byeoldori/data/model/dto/RefreshResponse.kt

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RefreshResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "token_type") val tokenType: String = "Bearer"
) {
    /** access token 값을 bearer 속성으로 얻기 */
    val bearer: String? get() = accessToken

    /** Authorization 헤더로 바로 쓸 수 있는 값 */
    val authHeader: String? get() = bearer?.let { "${tokenType ?: "Bearer"} $it" }
}