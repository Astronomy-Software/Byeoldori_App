package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.AuthApi
import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.dto.EmailCheckResponse
import com.example.byeoldori.data.model.dto.NicknameCheckResponse
import com.example.byeoldori.data.model.dto.SignUpRequest
import com.example.byeoldori.data.model.dto.SignUpResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignUpRepository @Inject constructor(
    private val authApi: AuthApi
) {
    /** 이메일 중복 확인 */
    suspend fun checkEmail(email: String): ApiResponse<EmailCheckResponse> {
        return authApi.checkEmailDuplicate(email)
    }

    /** 닉네임 중복 확인 */
    suspend fun checkNickname(nickname: String): ApiResponse<NicknameCheckResponse> {
        return authApi.checkNicknameDuplicate(nickname)
    }

    /** 이메일 인증 (토큰 확인) */
    suspend fun verifyEmail(token: String): ApiResponse<String> {
        return authApi.verifyEmail(token)
    }

    /** 회원가입 */
    suspend fun signUp(req: SignUpRequest): SignUpResponse {
        return authApi.signUp(req)
    }
}
