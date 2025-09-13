package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.dto.LoginRequest
import com.example.byeoldori.data.model.dto.LoginResponse
import com.example.byeoldori.data.model.dto.RefreshRequest
import com.example.byeoldori.data.model.dto.RefreshResponse
import com.example.byeoldori.data.model.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("users/me")
    suspend fun getMe(): UserDto

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Long): UserDto

    // 이 부분을 추가해야 합니다.
    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse
}
