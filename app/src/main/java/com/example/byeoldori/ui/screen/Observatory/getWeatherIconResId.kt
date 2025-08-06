package com.example.byeoldori.ui.screen.Observatory

import com.example.byeoldori.R

fun getWeatherIconResId(iconName: String): Int {
    return when (iconName) {
        "sunny" -> R.drawable.sun
        "rain" -> R.drawable.cloud_rain
        "snow" -> R.drawable.snowflake
        "cloud_sun" -> R.drawable.cloud_sun
        "cloud_moon" -> R.drawable.cloud_moon
        else -> R.drawable.sun
    }
}
