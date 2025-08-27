package com.example.byeoldori.viewmodel.Observatory

import com.example.byeoldori.R
import com.naver.maps.geometry.LatLng

enum class ObservatoryType {
    GENERAL, POPULAR
}

data class Observatory(
    val name: String,
    val type: ObservatoryType,
    val latLng: LatLng,
    val reviewCount: Int,
    val likeCount: Int,
    val avgRating: Float,
    val address: String,
    val imageRes: Int,
    val suitability: Int // 예: 87%
)

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
    // 원하는 만큼 더 추가
)