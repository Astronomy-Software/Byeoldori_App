package com.example.byeoldori.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.byeoldori.domain.Observatory.DailyForecast
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("weather_cache")

class WeatherDataCache (
    private val context: Context,
    moshi: Moshi
) : WeatherCache {

    private val type = Types.newParameterizedType(List::class.java, DailyForecast::class.java)
    private val adapter = moshi.adapter<List<DailyForecast>>(type) //JSON을 객체로 변환하는 어댑터

    private fun jsonKey (key: String): Preferences.Key<String> =
        stringPreferencesKey("daily_json_$key")

    private fun timeKey (key: String): Preferences.Key<Long> =
        longPreferencesKey("daily_time_$key")

    override suspend fun saveDaily(key: String, daily: List<DailyForecast>, updatedAt: Long) {
        val jsonString = adapter.toJson(daily)
        context.dataStore.edit { prefs ->
            prefs[jsonKey(key)] = jsonString
            prefs[timeKey(key)] = updatedAt
        } //이후 조회시 두 키를 함꼐 읽어 리스트와 최신 저장 시간을 가져옴
    }

    override suspend fun loadDaily(key: String): CachedDaily? {
        val prefs = context.dataStore.data.first() //현재 저장된 모든 key-value 맵
        val jsonString = prefs[jsonKey(key)] ?: return null //캐시에 저장된 JSON문자열을 꺼냄
        val time = prefs[timeKey(key)] ?: 0L //캐시 저장 시각
        val items = adapter.fromJson(jsonString) ?: return null
        return CachedDaily(items, time)
    }
}