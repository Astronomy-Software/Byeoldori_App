package com.example.byeoldori.data.remote.interceptor

import com.example.byeoldori.data.local.datastore.TokenDataStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 요청에 공통 헤더 + Authorization(Bearer) 자동 첨부
 */
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/json")

        // 이미 Authorization 헤더가 있으면 유지
        if (original.header("Authorization") == null) {
            tokenStore.getAuthHeader()?.let { builder.header("Authorization", it) }
        }

        return chain.proceed(builder.build())
    }
}
