// data/api/AuthApi.kt
package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.dto.FindEmailRequset
import com.example.byeoldori.data.model.dto.LoginRequest
import com.example.byeoldori.data.model.dto.LoginResponse
import com.example.byeoldori.data.model.dto.ResetPasswordRequest
import com.example.byeoldori.data.model.dto.ResetPasswordToEmailRequest
import com.example.byeoldori.data.model.dto.SignUpRequest
import com.example.byeoldori.data.model.dto.SignUpResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/logout")
    suspend fun logout(@Body body: LoginRequest): LoginResponse

    @POST("auth/signup")
    suspend fun signUp(@Body body: SignUpRequest): SignUpResponse

    @POST("auth/find-email")
    suspend fun findEmail(@Body body: FindEmailRequset): ApiResponse<String>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): ApiResponse<String>

    @POST("auth/reset-password-request")
    suspend fun resetPasswordToEmail(@Body body:ResetPasswordToEmailRequest): ApiResponse<String>

    @DELETE("auth/delete-email")
    suspend fun describeEmail(): ApiResponse<Any?>

}
