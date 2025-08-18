package com.example.byeoldori.ui.screen.Observatory

data class DailyForecast(
    val date: String,
    val precipitation: String,
    val amIcon: String,
    val pmIcon: String,
    val dayTemp: String,
    val nightTemp: String,
    val suitability: String
)
