package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.ApiService
import com.example.byeoldori.data.local.datastore.TokenDataStore
import com.example.byeoldori.data.model.dto.LoginRequest
import com.example.byeoldori.data.model.dto.LoginResponse
import com.example.byeoldori.data.model.dto.RefreshRequest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokens: TokenDataStore
) {
    /** 로그인 → 토큰 저장 */
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val res: LoginResponse = api.login(LoginRequest(email, password))
        val access = res.bearer ?: error("액세스 토큰이 없습니다.")
        val type = res.tokenType ?: "Bearer"
        tokens.save(accessToken = access, refreshToken = res.refreshToken, tokenType = type)
        Unit
    }

    /** 리프레시 토큰으로 갱신 */
    suspend fun refresh(): Result<Unit> = runCatching {
        val refresh = tokens.getRefreshToken() ?: error("리프레시 토큰이 없습니다.")
        val res = api.refresh(RefreshRequest(refresh))
        val access = res.bearer ?: error("액세스 토큰이 없습니다.")
        val type = res.tokenType ?: "Bearer"
        // 서버가 새 refreshToken을 주지 않으면 기존 값 유지
        tokens.save(accessToken = access, refreshToken = res.refreshToken ?: refresh, tokenType = type)
        Unit
    }

    /** 로그아웃(토큰 삭제) */
    suspend fun logout(): Result<Unit> = runCatching { tokens.clear() }

    /** 로그인 여부 스트림 (UI에서 관찰용) */
    val isLoggedInFlow: Flow<Boolean> = tokens.accessTokenFlow.map { !it.isNullOrBlank() }
}
