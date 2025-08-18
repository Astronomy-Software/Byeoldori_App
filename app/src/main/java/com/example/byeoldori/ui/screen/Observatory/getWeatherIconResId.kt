package com.example.byeoldori.ui.screen.Observatory

import com.example.byeoldori.R

fun getWeatherIconResId(iconName: String): Int {
    return when (iconName) {
        "sunny" -> R.drawable.sun1
        "rain" -> R.drawable.cloud_rain1
        "snow" -> R.drawable.snowflake1
        "cloud_sun" -> R.drawable.cloud_sun1
        "cloud_moon" -> R.drawable.cloud_moon1
        else -> R.drawable.sun1
    }
}
