// data/remote/interceptor/AuthInterceptor.kt
package com.example.byeoldori.data.remote.interceptor

import com.example.byeoldori.data.local.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val builder = original.newBuilder()
            .header("Accept", "application/json")
            // 항상 Authorization 헤더 초기화 (기존 값 제거)
            .removeHeader("Authorization")

        // DataStore에서 최신 값 가져오기
        val authHeader = runBlocking { tokenStore.authHeaderFlow.first() }

        if (!authHeader.isNullOrBlank()) {
            builder.header("Authorization", authHeader)
        }

        return chain.proceed(builder.build())
    }
}


