package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.dto.UpdateUserProfile
import com.example.byeoldori.data.model.dto.UserProfile
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApi {
    @GET("users/me")
    suspend fun getMyProfile(): ApiResponse<UserProfile>

    @PATCH("users/me")
    suspend fun updateMe(
        @Body body : UpdateUserProfile
    ): ApiResponse<Any?>

    @DELETE("users/me")
    suspend fun resign(): ApiResponse<Any?>

    @POST("users/logout")
    suspend fun logOut(): ApiResponse<Any?>
}