// data/remote/interceptor/RefreshTokenAuthenticator.kt
package com.example.byeoldori.data.remote.interceptor

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
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class RefreshTokenAuthenticator @Inject constructor(
    private val tokenStore: TokenDataStore,
    // ★ 재발급 전용 Retrofit에서 만든 RefreshApi 주입
    private val refreshApi: RefreshApi
) : Authenticator {

    private val refreshing = AtomicBoolean(false)

    override fun authenticate(route: Route?, response: Response): Request? {
        if (count(response) >= 2) return null
        if (!refreshing.compareAndSet(false, true)) return null

        return try {
            val newAccess = runBlocking {
                val rt = tokenStore.refreshToken() ?: return@runBlocking null
                val res: ApiResponse<TokenData> = refreshApi.refresh(RefreshRequest(rt))
                if (res.success && res.data != null) {
                    tokenStore.saveTokens(
                        access = res.data.accessToken,
                        refresh = res.data.refreshToken,
                        atExp = res.data.accessTokenExpiresAt,
                        rtExp = res.data.refreshTokenExpiresAt
                    )
                    res.data.accessToken
                } else null
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
            n++; cur = cur.priorResponse
        }
        return n
    }
}
