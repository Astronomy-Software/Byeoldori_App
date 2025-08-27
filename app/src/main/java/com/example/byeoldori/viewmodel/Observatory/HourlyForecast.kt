package com.example.byeoldori.viewmodel.Observatory

data class HourlyForecast(
    val date: String,       // 예: "5.23"
    val time: String,       // 예: "4시"
    val temperature: String,// 예: "15°"
    val iconName: String,   // 예: "sunny" → drawable 리소스 이름
    val precipitation: String,   // 예: "100%"
    val suitability: String // 예: "85%"
)
