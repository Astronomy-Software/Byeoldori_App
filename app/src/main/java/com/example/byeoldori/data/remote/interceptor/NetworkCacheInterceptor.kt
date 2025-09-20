package com.example.byeoldori.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * - 온라인: 짧게 재사용 (public, max-age)
 * - 오프라인: 캐시만 사용 (only-if-cached, max-stale)
 * - 인증/민감 엔드포인트는 캐시 제외 목록으로 걸러냄
 */
class NetworkCacheInterceptor @Inject constructor(
    private val isOnline: () -> Boolean,
    private val onlineMaxAgeSec: Int = 30,            // 온라인 시 30초 재사용
    private val offlineMaxStaleDays: Int = 7,         // 오프라인 시 7일 이내 캐시 허용
    private val noCachePaths: List<String> = listOf(  // 캐시 금지 경로 (필요시 추가)
        "/auth", "/login", "/logout", "/refresh", "/me"
    )
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()

        // 캐시 금지 경로면 그대로 진행
        val urlPath = req.url.encodedPath
        val shouldBypassCache = noCachePaths.any { urlPath.startsWith(it) }

        val newReq = if (shouldBypassCache) {
            req.newBuilder()
                .header("Cache-Control", "no-cache")
                .removeHeader("Pragma")
                .build()
        } else if (isOnline()) {
            // 온라인: GET 위주로 짧은 캐시 허용 (서버가 별도 정책 없을 때 유용)
            req.newBuilder()
                .header("Cache-Control", "public, max-age=$onlineMaxAgeSec")
                .removeHeader("Pragma")
                .build()
        } else {
            // 오프라인: 캐시만 사용
            val maxStaleSec = TimeUnit.DAYS.toSeconds(offlineMaxStaleDays.toLong())
            req.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStaleSec")
                .removeHeader("Pragma")
                .build()
        }

        val resp = chain.proceed(newReq)

        // 서버가 Cache-Control을 주지 않는 경우(혹은 no-store) 보수적으로 보정
        if (!shouldBypassCache && isOnline()) {
            val cacheControl = resp.header("Cache-Control") ?: ""
            val hasUsefulCache = cacheControl.contains("max-age") || cacheControl.contains("s-maxage")
            if (!hasUsefulCache && req.method.equals("GET", ignoreCase = true)) {
                return resp.newBuilder()
                    .header("Cache-Control", "public, max-age=$onlineMaxAgeSec")
                    .removeHeader("Pragma")
                    .build()
            }
        }
        return resp
    }
}
