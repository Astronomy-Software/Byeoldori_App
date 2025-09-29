package com.example.byeoldori.viewmodel.Observatory

data class MarkerInfo(
    val name: String,
    val type: ObservatoryType,
    val reviewCount: Int,
    val likeCount: Int,
    val rating: Float,
    val suitability: Int,
    val address: String,
    val drawableRes: Int,
    val latitude: Double,
    val longitude: Double
)
