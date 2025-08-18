package com.example.byeoldori.ui.screen.Observatory

import com.example.byeoldori.R

fun getWeatherIconResId(iconName: String): Int {
    return when (iconName) {
        "sunny" -> R.drawable.ic_sun
        "rain" -> R.drawable.ic_cloud_rain
        "snow" -> R.drawable.ic_snowflake
        "cloud_sun" -> R.drawable.ic_cloud_sun
        "cloud_moon" -> R.drawable.ic_cloud_moon
        else -> R.drawable.ic_sun
    }
}
