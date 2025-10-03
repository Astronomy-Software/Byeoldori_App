package com.example.byeoldori.ui.components.observatory

import com.example.byeoldori.R

fun getWeatherIconResId(iconName: String): Int {
    return when (iconName) {
        "sunny" -> R.drawable.ic_sun
        "rain" -> R.drawable.ic_cloud_rain
        "snow" -> R.drawable.ic_snowflake
        "cloud_sun" -> R.drawable.ic_cloud_sun
        "cloud_moon" -> R.drawable.ic_cloud_moon
        "cloud" -> R.drawable.ic_cloudy
        "cloud_snow" -> R.drawable.ic_cloud_snow
        "moon" -> R.drawable.ic_moon_star
        else -> R.drawable.ic_sun
    }
}
/*
==========================================
단기 예보 아이콘 매핑
==========================================
하늘상태(Sky) 구분은 맑음(1), 구름조금(2), 구름많음(3), 흐림(4)**
강수형태(PTY) 코드 : 없음(0), 비(1), 비/눈(2), 눈(3), 빗방울(5), 빗방울눈날림(6), 눈날림(7)**
*/
fun shortSkytoIcon(sky:Int, day:Boolean): String = when (sky) {
    1 -> if(day) "sunny" else "moon" //맑음
    2 -> if(day) "cloud_sun" else "cloud_moon" //구름 조금
    3 -> if(day) "cloud_sun" else "cloud_moon" //구름 많음
    4 -> "cloud" //흐림
    else -> if(day) "cloud_sun" else "cloud_moon"
}

fun shortPtytoIcon(pty:Int): String? = when (pty) {
    1 -> "rain" // 비
    2 -> "cloud_snow" // 비/눈
    3 -> "snow" // 눈
    5 -> "rain" // 빗방울
    6 -> "cloud_snow" //빗방울 눈날림
    7 -> "snow" //눈날림
    else -> null //0일 경우 강수 없음
}

fun shortWeatherIcon(sky:Int, pty:Int, day:Boolean): String {
    return  shortPtytoIcon(pty) ?: shortSkytoIcon(sky, day)
}

/*
==========================================
중기 예보 아이콘 매핑
==========================================
*/

fun midSkytoIcon(sky:String, day:Boolean): String = when (sky) {
    "WB01" -> if(day) "sunny" else "moon" //맑음
    "WB02" -> if(day) "cloud_sun" else "cloud_moon" //구름 조금
    "WB03" -> if(day) "cloud_sun" else "cloud_moon" //구름 많음
    "WB04" -> "cloud" //흐림
    else -> if(day) "cloud_sun" else "cloud_moon"
}

fun midPretoIcon(pre:String): String? = when (pre) {
    "WB09" -> "rain" // 비
    "WB10" -> "rain" //소나기
    "WB11" -> "cloud_snow" // 비/눈
    "WB12" -> "snow" // 눈
    "WB13" -> "cloud_snow" // 눈/비
    else -> null // WB00일때는 강수 없음
}

fun midWeatherIcon(sky:String, pre:String, day:Boolean): String {
    return midPretoIcon(pre) ?: midSkytoIcon(sky, day) //강수를 우선으로
}