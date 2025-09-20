package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.dto.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather/ForecastData")
    suspend fun getForecastData(
        @Query("lat") lat: Double,
        @Query("long") long: Double
    ): ForecastResponse
}