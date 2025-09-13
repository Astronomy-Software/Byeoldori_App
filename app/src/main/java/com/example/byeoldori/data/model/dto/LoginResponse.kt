package com.example.byeoldori.data.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    // 일부 서버: { "token": "..." }
    val token: String? = null,

    // OAuth 스타일: { "access_token": "...", "refresh_token": "...", "token_type": "Bearer", "expires_in": 3600 }
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "token_type")   val tokenType: String? = "Bearer",
    @Json(name = "expires_in")   val expiresIn: Long? = null,

    // 로그인과 함께 내려오는 유저 정보(옵션)
    val user: UserDto? = null
) {
    /** access token(혹은 token)만 순수 값으로 얻기 */
    val bearer: String? get() = token ?: accessToken

    /** Authorization 헤더로 바로 쓸 수 있는 값 */
    val authHeader: String? get() = bearer?.let { "${tokenType ?: "Bearer"} $it" }
}
