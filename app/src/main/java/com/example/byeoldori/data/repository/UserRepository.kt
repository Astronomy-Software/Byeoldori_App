package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.UserApi
import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.dto.UpdateUserProfile
import com.example.byeoldori.data.model.dto.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
) {
    /** 내 프로필 조회 */
    suspend fun getMyProfile(): ApiResponse<UserProfile> {
        return api.getMyProfile()
    }

    /** 내 프로필 수정 */
    suspend fun updateMe(profile: UpdateUserProfile): ApiResponse<Any?> {
        return api.updateMe(profile)
    }
    /** 회원 탈퇴 */
    suspend fun resign(): ApiResponse<Any?> {
        return api.resign()
    }

    /** 로그아웃 */
    suspend fun logOut(): ApiResponse<Any?> {
        return api.logOut()
    }
}

