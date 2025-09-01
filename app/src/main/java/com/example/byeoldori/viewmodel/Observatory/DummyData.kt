package com.example.byeoldori.viewmodel.Observatory

import com.example.byeoldori.R
import com.naver.maps.geometry.LatLng

val observatoryList = listOf(
    Observatory(
        name = "오산천",
        type = ObservatoryType.POPULAR,
        latLng = LatLng(37.1570906, 127.0703307),
        reviewCount = 103,
        likeCount = 57,
        avgRating = 4.3f,
        address = "경기도 오산시 오산천로 254-5",
        imageRes = R.drawable.img_dummy,
        suitability = 87
    ),
    Observatory(
        name = "필봉산",
        type = ObservatoryType.GENERAL,
        latLng = LatLng(37.179097404593584, 127.07212073869198),
        reviewCount = 150,
        likeCount = 99,
        avgRating = 4.2f,
        address = "경기도 오산시 내삼미동 산 21-1",
        imageRes = R.drawable.img_dummy,
        suitability = 85
    ),
    Observatory(
        name = "배티공원",
        type = ObservatoryType.GENERAL,
        latLng = LatLng(36.6266086351, 127.4653234453252),
        reviewCount = 69,
        likeCount = 12,
        avgRating = 3.9f,
        address = "충청북도 청주시 서원구 개신동 3-16",
        imageRes = R.drawable.img_dummy,
        suitability = 88
    ),
    Observatory(
        name = "구룡산",
        type = ObservatoryType.POPULAR,
        latLng = LatLng(36.61834002153799, 127.46435709201829),
        reviewCount = 79,
        likeCount = 150,
        avgRating = 4.8f,
        address = "충북 청주시 상당구 문의면 덕유리",
        imageRes = R.drawable.img_dummy,
        suitability = 92
    ),
)

//현재 날씨 더미
val dummyCurrentWeather = CurrentWeather(
    temperature = "14°",
    humidity = "35%",
    windSpeed = "→ 3m/s",
    suitability = "75%"
)

//날씨 (시간별) 더미 데이터
val dummyHourlyForecasts = listOf(
    HourlyForecast("5.23", "4시", "15°", "cloud_sun", "60%", "85%"),
    HourlyForecast("5.23", "5시", "16°", "sunny",     "55%", "82%"),
    HourlyForecast("5.23", "6시", "17°", "rain",      "70%", "60%"),
    HourlyForecast("5.24", "1시", "13°", "cloud_moon","80%", "90%"),
    HourlyForecast("5.24", "2시", "12°", "cloud_sun", "85%", "88%"),
    HourlyForecast("5.24", "3시", "14°", "sunny",     "60%", "60%")
    // ...
)

//날씨 (일별) 더미 데이터
val dummyDailyForecasts = listOf(
    DailyForecast("5.27", "100%", "sunny",      "cloud",      "27°", "13°", "85%"),
    DailyForecast("5.28", "80%",  "cloud",      "rain",       "25°", "12°", "60%"),
    DailyForecast("5.29", "90%",  "rain",       "rain",       "23°", "11°", "45%"),
    DailyForecast("5.30", "100%", "rain",       "rain",       "22°", "10°", "20%"),
    DailyForecast("5.31", "100%", "cloud_sun",  "rain",       "23°", "9°",  "40%"),
    DailyForecast("6.1",  "100%", "rain",       "cloud_moon", "22°", "11°", "35%")
    // ...
)

//리뷰 더미 데이터
val dummyReviews = listOf(
    Review("2", "토성 고리 봄", "아이마카", 5.0f, 20, 5, R.drawable.img_dummy),
    Review("3", "목성 위성 본 날", "아이마카", 5.0f, 40, 8, R.drawable.img_dummy),
    Review("4", "태양 흑점 본 날", "아이마카", 5.0f, 30, 10, R.drawable.img_dummy),
    Review("5", "태양 흑점 본 날", "아이마카", 5.0f, 30, 10, R.drawable.img_dummy),
    Review("6", "태양 흑점 본 날", "아이마카", 5.0f, 30, 10, R.drawable.img_dummy),
    Review("7", "태양 흑점 본 날", "아이마카", 5.0f, 30, 10, R.drawable.img_dummy)
)