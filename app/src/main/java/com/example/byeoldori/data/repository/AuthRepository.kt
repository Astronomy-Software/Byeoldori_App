package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.AuthApi
import com.example.byeoldori.data.api.RefreshApi
import com.example.byeoldori.data.api.UserApi
import com.example.byeoldori.data.local.datastore.TokenDataStore
import com.example.byeoldori.data.model.common.TokenData
import com.example.byeoldori.data.model.dto.LoginRequest
import com.example.byeoldori.data.model.dto.RefreshRequest
import com.example.byeoldori.data.model.dto.SignUpRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,          // /auth/login, /auth/signup
    private val refreshApi: RefreshApi,    // /auth/token
    private val userApi: UserApi,          // /users/logout
    private val tokenStore: TokenDataStore // 영속 저장
) {

    suspend fun login(email: String, password: String): TokenData {
        val resp = authApi.login(LoginRequest(email, password))
        if (!resp.success) throw Exception(resp.message)

        val t = resp.data ?: throw Exception("토큰 데이터가 null 입니다.")
        persistToken(t)
        return t
    }

    suspend fun signUp(req: SignUpRequest) {
        val resp = authApi.signUp(req)
        if (!resp.success) throw Exception(resp.message)
    }

    suspend fun refresh(): TokenData {
        val rt = tokenStore.refreshToken() ?: throw Exception("리프레시 토큰 없음")
        val resp = refreshApi.refresh(RefreshRequest(rt))
        if (!resp.success) throw Exception(resp.message)

        val t = resp.data ?: throw Exception("토큰 데이터가 null 입니다.")
        persistToken(t)
        return t
    }

    suspend fun logout() {
        try {
            val resp = userApi.logOut()
            if (!resp.success) {
                // 서버 실패 로그만 남기고 로컬은 반드시 clear
                throw Exception(resp.message)
            }
        } catch (e: Exception) {
            // 네트워크 실패해도 로컬 토큰은 무조건 삭제
        } finally {
            tokenStore.clear()
        }
    }

    // ──────────────────────────────
    // private helper
    // ──────────────────────────────
    private suspend fun persistToken(t: TokenData) {
        tokenStore.saveTokens(
            access    = t.accessToken,
            refresh   = t.refreshToken,
            atExp     = t.accessTokenExpiresAt,
            rtExp     = t.refreshTokenExpiresAt,
            tokenType = "Bearer"
        )
    }
}
