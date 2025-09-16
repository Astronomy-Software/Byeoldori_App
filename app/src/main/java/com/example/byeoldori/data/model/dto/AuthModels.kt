package com.example.byeoldori.data.model.dto

import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.common.TokenData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// data/model/auth/AuthModels.kt (혹은 SignUpModels.kt)
@JsonClass(generateAdapter = true)
data class SignUpRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val name: String,
    val phone: String,
    val nickname: String,
//    val birthdate: String   // yyyy-MM-dd 형식
)

// 회원가입 응답은 data = null → Unit? 사용 가능
typealias SignUpResponse = ApiResponse<Unit?>

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