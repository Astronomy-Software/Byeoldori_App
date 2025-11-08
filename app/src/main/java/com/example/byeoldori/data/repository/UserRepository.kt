package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.UserApi
import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.dto.UpdateUserProfile
import com.example.byeoldori.data.model.dto.UserProfile
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun getMyProfile(): ApiResponse<UserProfile> {
        return api.getMyProfile()
    }

    suspend fun updateMe(profile: UpdateUserProfile): ApiResponse<Any?> {
        return api.updateMe(profile)
    }
    suspend fun resign(): ApiResponse<Any?> {
        return api.resign()
    }

    suspend fun logOut(): ApiResponse<Any?> {
        return api.logOut()
    }
    suspend fun uploadProfileImage(part: MultipartBody.Part) =
        api.uploadProfileImage(part)

}

