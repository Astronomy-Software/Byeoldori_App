package com.example.byeoldori.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

// 이 파일 하나에서만 선언하세요. (중복 선언 금지)
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")
    private val KEY_TOKEN_TYPE = stringPreferencesKey("token_type") // 기본 Bearer

    /** 액세스/리프레시 토큰 저장 */
    suspend fun save(
        accessToken: String,
        refreshToken: String? = null,
        tokenType: String = "Bearer"
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = accessToken
            prefs[KEY_TOKEN_TYPE] = tokenType
            if (refreshToken != null) prefs[KEY_REFRESH] = refreshToken
            else prefs.remove(KEY_REFRESH)
        }
    }

    /** 모두 삭제(로그아웃 등) */
    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS)
            prefs.remove(KEY_REFRESH)
            prefs.remove(KEY_TOKEN_TYPE)
        }
    }

    /** 비동기 스트림들 */
    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_ACCESS] }
    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_REFRESH] }
    val tokenTypeFlow:  Flow<String>    = context.dataStore.data.map { it[KEY_TOKEN_TYPE] ?: "Bearer" }
    val authHeaderFlow: Flow<String?>   = context.dataStore.data.map {
        val token = it[KEY_ACCESS] ?: return@map null
        val type  = it[KEY_TOKEN_TYPE] ?: "Bearer"
        "$type $token"
    }

    /** 인터셉터 등 동기 컨텍스트에서 사용(가벼운 조회만) */
    fun getAccessToken(): String? = runBlocking { accessTokenFlow.first() }
    fun getRefreshToken(): String? = runBlocking { refreshTokenFlow.first() }
    fun getAuthHeader():  String? = runBlocking { authHeaderFlow.first() }
}
