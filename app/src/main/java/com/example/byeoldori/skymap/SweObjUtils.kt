package com.example.byeoldori.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * SweObjUtils
 * - Stellarium Web Object 이름 ↔ 한국어 이름 변환 유틸리티
 * - "NAME Deneb" ↔ "데네브"
 * - "* h1 Star" ↔ "별 1"
 * - JSON 기반 캐시
 *
 * ⚠️ 입력 문자열을 변형하지 않음 (접두어 제거 X)
 */
object SweObjUtils {

    private val sweToKor = mutableMapOf<String, String>()
    private val korToSwe = mutableMapOf<String, String>()
    private var initialized = false

    /**
     * JSON 로드 (앱 시작 시 1회)
     */
    suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        if (initialized) return@withContext

        val jsonStr = context.assets.open("swe_objects.json")
            .bufferedReader(StandardCharsets.UTF_8)
            .use { it.readText() }

        val jsonObject = JSONObject(jsonStr)
        jsonObject.keys().forEach { sweName ->
            val korName = jsonObject.getString(sweName)
            sweToKor[sweName.trim()] = korName.trim()
            korToSwe[korName.trim()] = sweName.trim()
        }

        initialized = true
    }

    /**
     * SWE Object 이름 → 한국어 이름
     * 예: "NAME Deneb" → "데네브"
     */
    fun toKorean(sweName: String): String {
        if (!initialized) return sweName
        return sweToKor[sweName.trim()] ?: sweName
    }

    /**
     * 한국어 이름 → SWE Object 이름
     * 예: "데네브" → "NAME Deneb"
     */
    fun toSweFormat(korName: String): String {
        if (!initialized) return korName
        return korToSwe[korName.trim()] ?: korName
    }
    fun isReady(): Boolean = initialized
}