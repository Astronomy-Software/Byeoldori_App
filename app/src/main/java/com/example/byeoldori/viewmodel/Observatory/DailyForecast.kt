package com.example.byeoldori.viewmodel.Observatory

data class DailyForecast(
    val date: String,
    val precipitation: String,
    val amIcon: String,
    val pmIcon: String,
    val dayTemp: String,
    val nightTemp: String,
    val suitability: String
)
