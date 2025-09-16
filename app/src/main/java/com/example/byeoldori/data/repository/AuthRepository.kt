package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.AuthApi
import com.example.byeoldori.data.api.RefreshApi
import com.example.byeoldori.data.local.datastore.TokenDataStore
import com.example.byeoldori.data.model.common.TokenData   // ✅ 공용 TokenData
import com.example.byeoldori.data.model.dto.LoginRequest
import com.example.byeoldori.data.model.dto.RefreshRequest
import com.example.byeoldori.data.model.dto.SignUpRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,          // 비인증: /auth/login, /auth/signup
    private val refreshApi: RefreshApi,    // 재발급 전용: /auth/token
    private val tokenStore: TokenDataStore // 영속 저장
) {
    suspend fun login(email: String, password: String):TokenData {
        val envelope = authApi.login(LoginRequest(email, password))
        if (!envelope.success) throw Exception(envelope.message)

        // 🔧 엘비스 제거 (LoginResponse = ApiResponse<TokenData> → data는 non-null)
        val t: TokenData = envelope.data

        tokenStore.saveTokens(
            access    = t.accessToken,
            refresh   = t.refreshToken,
            atExp     = t.accessTokenExpiresAt,     // 필요시 millis 변환 함수 사용
            rtExp     = t.refreshTokenExpiresAt,
            tokenType = "Bearer"
        )
        return t
    }

    suspend fun signUp(req: SignUpRequest) {
        val envelope = authApi.signUp(req)
        if (!envelope.success) throw Exception(envelope.message)
    }

    suspend fun refresh(): TokenData {
        val rt = tokenStore.refreshToken() ?: throw Exception("리프레시 토큰 없음")

        val envelope = refreshApi.refresh(RefreshRequest(rt))
        if (!envelope.success) throw Exception(envelope.message)

        // 🔧 엘비스 제거 (RefreshResponse = ApiResponse<TokenData>)
        val t: TokenData = envelope.data

        tokenStore.saveTokens(
            access    = t.accessToken,
            refresh   = t.refreshToken,
            atExp     = t.accessTokenExpiresAt,
            rtExp     = t.refreshTokenExpiresAt,
            tokenType = "Bearer"
        )
        return t
    }

    suspend fun logout() {
        tokenStore.clear()
    }
}
