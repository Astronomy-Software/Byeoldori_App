package com.example.byeoldori.viewmodel.Observatory

import java.time.LocalDate


data class DailyForecast(
    val date: String,
    val precipitation: String,
    val amIcon: String,
    val pmIcon: String,
    val dayTemp: String,
    val nightTemp: String,
    val suitability: String
)

