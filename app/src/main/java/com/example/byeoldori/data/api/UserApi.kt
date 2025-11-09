package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.dto.ProfileImageResponse
import com.example.byeoldori.data.model.dto.UpdateUserProfile
import com.example.byeoldori.data.model.dto.UserProfile
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface UserApi {
    @GET("users/me")
    suspend fun getMyProfile(): ApiResponse<UserProfile>

    @PATCH("users/me")
    suspend fun updateMe(
        @Body body : UpdateUserProfile
    ): ApiResponse<Any?>

    @DELETE("users/me") // 회원 탈퇴
    suspend fun resign(): ApiResponse<Any?>

    @POST("users/logout")
    suspend fun logOut(): ApiResponse<Any?>

    @Multipart
    @POST("users/me/profile-image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): ApiResponse<ProfileImageResponse>
}