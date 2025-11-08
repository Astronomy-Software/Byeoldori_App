package com.example.byeoldori.eduprogram

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonLoader @Inject constructor(
    private val okHttp: OkHttpClient
) {
    suspend fun loadFromUrl(url: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            okHttp.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("❌ JSON 요청 실패: ${response.code}")
                    return@withContext null
                }
                val body = response.body?.string() ?: return@withContext null
                JSONObject(body)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
