package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.dto.RefreshRequest
import com.example.byeoldori.data.model.dto.RefreshResponse
import retrofit2.http.Body
import retrofit2.http.POST

// refresh token 발급받는경우는 따로 분리해두어야 충돌이 일어나지않아 나누어둠.

interface RefreshApi {
    @POST("auth/token")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse
}
