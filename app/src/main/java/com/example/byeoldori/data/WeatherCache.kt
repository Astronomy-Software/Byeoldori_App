package com.example.byeoldori.data

import com.example.byeoldori.domain.Observatory.DailyForecast

interface WeatherCache {
    suspend fun saveDaily(key: String, daily: List<DailyForecast>, updatedAt: Long)
    suspend fun loadDaily(key: String): CachedDaily?
}

data class CachedDaily(
    val items: List<DailyForecast>, //캐시된 리스트와 최근 업데이트 시간을 가지고 있음
    val updatedAt: Long
)