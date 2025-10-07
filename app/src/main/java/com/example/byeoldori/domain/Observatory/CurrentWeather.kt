package com.example.byeoldori.domain.Observatory

data class CurrentWeather(
    val temperature: String,
    val humidity: String,
    val windSpeed: String,
    val suitability: String,
    val windDirection: Int
)
