package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.AuthApi
import com.example.byeoldori.data.api.RefreshApi
import com.example.byeoldori.data.local.datastore.TokenDataStore
import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.common.TokenData
import com.example.byeoldori.data.model.dto.FindEmailRequest
import com.example.byeoldori.data.model.dto.LoginRequest
import com.example.byeoldori.data.model.dto.RefreshRequest
import com.example.byeoldori.data.model.dto.ResetPasswordToEmailRequest
import com.example.byeoldori.data.model.dto.SignUpRequest
import com.example.byeoldori.data.model.dto.SignUpResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,          // /auth/login, /auth/signup
    private val refreshApi: RefreshApi,    // /auth/token
    private val tokenStore: TokenDataStore // 영속 저장
) {
    // Login 관련 - Login , Refrsh ,
    val isLoggedInFlow: Flow<Boolean> = tokenStore.isLoggedInFlow
    suspend fun login(email: String, password: String): TokenData {
        val resp = authApi.login(LoginRequest(email, password))
        if (!resp.success) throw Exception(resp.message)

        val t = resp.data ?: throw Exception("토큰 데이터가 null 입니다.")
        persistToken(t)
        return t
    }

    suspend fun refresh(): TokenData {
        val rt = tokenStore.refreshToken() ?: throw Exception("리프레시 토큰 없음")
        val resp = refreshApi.refresh(RefreshRequest(rt))
        if (!resp.success) throw Exception(resp.message)

        val t = resp.data ?: throw Exception("토큰 데이터가 null 입니다.")
        persistToken(t)
        return t
    }

    private suspend fun persistToken(t: TokenData) {
        tokenStore.saveTokens(
            access    = t.accessToken,
            refresh   = t.refreshToken,
            atExp     = t.accessTokenExpiresAtMillis(),
            rtExp     = t.refreshTokenExpiresAtMillis(),
            tokenType = "Bearer"
        )
    }

    // Email 인증 , 회원가입
    suspend fun verifyEmail(token: String): ApiResponse<String> {
        return authApi.verifyEmail(token)
    }

    suspend fun signUp(req: SignUpRequest): SignUpResponse {
        return authApi.signUp(req)
    }

    // Email 찾기, List<String> 반환
    suspend fun findEmail(req: FindEmailRequest): List<String> {
        val resp = authApi.findEmail(req)
        if (!resp.success) throw Exception(resp.message)
        return resp.data?.ids ?: emptyList()
    }
    // 비밀번호 초기화
    suspend fun resetPasswordToEmail(req: ResetPasswordToEmailRequest): ApiResponse<String> {
        return authApi.resetPasswordToEmail(req)
    }
}
