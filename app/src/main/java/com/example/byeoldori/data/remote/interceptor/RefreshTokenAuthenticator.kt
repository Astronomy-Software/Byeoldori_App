package com.example.byeoldori.data.remote.interceptor

import android.util.Log
import com.example.byeoldori.data.api.RefreshApi
import com.example.byeoldori.data.local.datastore.TokenDataStore
import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.common.TokenData
import com.example.byeoldori.data.model.dto.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class RefreshTokenAuthenticator @Inject constructor(
    private val tokenStore: TokenDataStore,
    private val refreshApi: RefreshApi
) : Authenticator {

    private val refreshing = AtomicBoolean(false)

    override fun authenticate(route: Route?, response: Response): Request? {
        if (count(response) >= 2) return null
        if (!refreshing.compareAndSet(false, true)) return null

        return try {
            val newAccess = runBlocking {
                try {
                    val refreshToken = tokenStore.refreshToken()
                    if (refreshToken.isNullOrBlank()) {
                        Log.w(TAG, "No refresh token found. Logging out.")
                        tokenStore.clear() // 🔸 로그아웃 처리
                        return@runBlocking null
                    }

                    val res: ApiResponse<TokenData> =
                        refreshApi.refresh(RefreshRequest(refreshToken))

                    if (res.success && res.data != null) {
                        Log.i(TAG, "✅ Access token successfully refreshed.")
                        val newData = res.data

                        tokenStore.saveTokens(
                            access = newData.accessToken,
                            refresh = newData.refreshToken,
                            atExp = newData.accessTokenExpiresAtMillis(),
                            rtExp = newData.refreshTokenExpiresAtMillis()
                        )

                        newData.accessToken
                    } else {
                        Log.e(TAG, "❌ Refresh failed: ${res.message ?: "Unknown error"}")
                        tokenStore.clear() // 🔸 토큰 불일치 → 로그아웃 처리
                        null
                    }

                } catch (e: IOException) {
                    Log.e(TAG, "❌ Network error during refresh: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Unexpected error during token refresh: ${e.message}")
                    e.printStackTrace()
                    // 서버/파싱 오류 → 토큰 초기화
                    runBlocking { tokenStore.clear() }
                    null
                }
            } ?: return null

            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccess")
                .build()
        } finally {
            refreshing.set(false)
        }
    }

    private fun count(r: Response): Int {
        var cur: Response? = r
        var n = 1
        while (cur?.priorResponse != null) {
            n++
            cur = cur.priorResponse
        }
        return n
    }

    companion object {
        private const val TAG = "RefreshTokenAuth"
    }
}
