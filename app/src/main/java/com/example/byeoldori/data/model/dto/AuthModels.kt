package com.example.byeoldori.data.model.dto

import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.common.TokenData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignUpRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val name: String,
    val phone: String,
    val consents: Consents
) {
    @JsonClass(generateAdapter = true)
    data class Consents(
        val termsOfService: Boolean,
        val privacyPolicy: Boolean,
        val marketing: Boolean,
        val location: Boolean
    )
}

typealias SignUpResponse = ApiResponse<Any?>

@JsonClass(generateAdapter = true)
data class RefreshRequest(
    @Json(name = "refreshToken") val refreshToken: String
)

typealias RefreshResponse = ApiResponse<TokenData>


@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

typealias LoginResponse = ApiResponse<TokenData>

@JsonClass(generateAdapter = true)
data class FindEmailRequest(
    val name: String,
    val phone: String
)

typealias FindEmailResponse = ApiResponse<FindEmailData>

@JsonClass(generateAdapter = true)
data class FindEmailData(
    val ids: List<String>
)

@JsonClass(generateAdapter = true)
data class ResetPasswordToEmailRequest(
    val email: String,
    val name: String,
    val phone: String
)
