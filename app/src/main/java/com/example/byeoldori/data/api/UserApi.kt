// data/api/UserApi.kt
package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.common.ApiResponse
import com.squareup.moshi.JsonClass
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApi {
    @GET("users/me")
    suspend fun getMyProfile(): ApiResponse<UserProfile>

    @PATCH("users/me")
    suspend fun updateMe(): ApiResponse<Unit?>

    @DELETE("users/me")
    suspend fun resign(): ApiResponse<Unit?>

    @POST("users/logout")
    suspend fun logOut(): ApiResponse<Unit?>
}

@JsonClass(generateAdapter = true)
data class UserProfile(
    val nickname: String?,
    val birthdate: String? // yyyy-MM-dd
)
