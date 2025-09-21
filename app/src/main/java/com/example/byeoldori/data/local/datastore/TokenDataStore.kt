package com.example.byeoldori.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

// ⚠️ 이 파일 하나에서만 선언하세요. (중복 선언 금지)
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_ACCESS       = stringPreferencesKey("access_token")
    private val KEY_REFRESH      = stringPreferencesKey("refresh_token")
    private val KEY_TOKEN_TYPE   = stringPreferencesKey("token_type") // 기본 Bearer
    private val KEY_AT_EXPIRES   = longPreferencesKey("access_token_expires_at")
    private val KEY_RT_EXPIRES   = longPreferencesKey("refresh_token_expires_at")

    // ------------------------------
    // 저장/삭제
    // ------------------------------

    /**
     * 액세스/리프레시 토큰 + 만료시각 저장
     * @param access 토큰 문자열
     * @param refresh 토큰 문자열(없으면 null)
     * @param atExp 액세스 토큰 만료시각 (epochMillis 권장)  ← 서버 단위와 동일해야 함!
     * @param rtExp 리프레시 토큰 만료시각 (epochMillis 권장)
     * @param tokenType "Bearer" 등
     */
    suspend fun saveTokens(
        access: String,
        refresh: String?,
        atExp: Long?,
        rtExp: Long?,
        tokenType: String = "Bearer"
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = access
            prefs[KEY_TOKEN_TYPE] = tokenType

            if (refresh != null) prefs[KEY_REFRESH] = refresh else prefs.remove(KEY_REFRESH)
            if (atExp != null)   prefs[KEY_AT_EXPIRES] = atExp   else prefs.remove(KEY_AT_EXPIRES)
            if (rtExp != null)   prefs[KEY_RT_EXPIRES] = rtExp   else prefs.remove(KEY_RT_EXPIRES)
        }
    }

    /**
     * 액세스 토큰만 갱신(재발급 성공 후 RT 그대로인 경우)
     */
    suspend fun saveAccessToken(
        access: String,
        atExp: Long? = null,
        tokenType: String = "Bearer"
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = access
            prefs[KEY_TOKEN_TYPE] = tokenType
            if (atExp != null) prefs[KEY_AT_EXPIRES] = atExp
        }
    }

    /** 모두 삭제(로그아웃 등) */
    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS)
            prefs.remove(KEY_REFRESH)
            prefs.remove(KEY_TOKEN_TYPE)
            prefs.remove(KEY_AT_EXPIRES)
            prefs.remove(KEY_RT_EXPIRES)
        }
        // 🔥 디버그 로그: 삭제 직후 상태 출력
        val access = runBlocking { accessTokenFlow.first() }
        val refresh = runBlocking { refreshTokenFlow.first() }
        val type = runBlocking { tokenTypeFlow.first() }
        val atExp = runBlocking { accessTokenExpiresAtFlow.first() }
        val rtExp = runBlocking { refreshTokenExpiresAtFlow.first() }

        println("🔥 TokenDataStore.clear() called")
        println("   accessToken = $access")
        println("   refreshToken = $refresh")
        println("   tokenType = $type")
        println("   accessTokenExpiresAt = $atExp")
        println("   refreshTokenExpiresAt = $rtExp")
    }

    // ------------------------------
    // Flow (비동기)
    // ------------------------------

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_ACCESS] }
    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_REFRESH] }
    val tokenTypeFlow:  Flow<String>    = context.dataStore.data.map { it[KEY_TOKEN_TYPE] ?: "Bearer" }

    val accessTokenExpiresAtFlow: Flow<Long?> =
        context.dataStore.data.map { it[KEY_AT_EXPIRES] }

    val refreshTokenExpiresAtFlow: Flow<Long?> =
        context.dataStore.data.map { it[KEY_RT_EXPIRES] }

    val authHeaderFlow: Flow<String?> = context.dataStore.data.map {
        val token = it[KEY_ACCESS] ?: return@map null
        val type  = it[KEY_TOKEN_TYPE] ?: "Bearer"
        "$type $token"
    }
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_ACCESS]?.isNotBlank() == true
    }

    // ------------------------------
    // 동기 조회(Interceptor/Authenticator에서 가볍게 씀)
    // ------------------------------

    /** 현재 AccessToken */
    fun getAccessToken(): String? = runBlocking { accessTokenFlow.first() }

    /** 현재 RefreshToken */
    fun refreshToken(): String? = runBlocking { refreshTokenFlow.first() } // ← Authenticator에서 사용

    /** "Bearer xxx" 형태 Authorization 헤더 */
    fun getAuthHeader(): String? = runBlocking { authHeaderFlow.first() }

    /** 액세스 토큰 만료시각 (epochMillis 권장) */
    fun accessTokenExpiresAt(): Long? = runBlocking { accessTokenExpiresAtFlow.first() }

    /** 리프레시 토큰 만료시각 (epochMillis 권장) */
    fun refreshTokenExpiresAt(): Long? = runBlocking { refreshTokenExpiresAtFlow.first() }

    // ------------------------------
    // (선택) 편의 함수
    // ------------------------------

    /** 액세스 토큰이 만료되었는지(서버/클라 시계 오차 고려해 여유 ms 적용 가능) */
    fun isAccessTokenExpired(nowMillis: Long = System.currentTimeMillis(), leewayMs: Long = 5_000): Boolean {
        val exp = accessTokenExpiresAt() ?: return false // 없으면 판단 불가 → 일단 false
        return nowMillis + leewayMs >= exp
    }

    /** 리프레시 토큰이 만료되었는지 */
    fun isRefreshTokenExpired(nowMillis: Long = System.currentTimeMillis(), leewayMs: Long = 5_000): Boolean {
        val exp = refreshTokenExpiresAt() ?: return false
        return nowMillis + leewayMs >= exp
    }
}
