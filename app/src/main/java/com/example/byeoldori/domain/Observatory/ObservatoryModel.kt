package com.example.byeoldori.domain.Observatory

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
