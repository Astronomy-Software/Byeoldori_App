package com.example.byeoldori.domain.Observatory

data class MarkerInfo(
    val name: String,
    val type: ObservatoryType? = null,
    val reviewCount: Int,
    val likeCount: Int,
    val rating: Float,
    val suitability: Int,
    val address: String,
    val drawableRes: Int? = null,
    val latitude: Double,
    val longitude: Double,
    val observationSiteId: Long? = null
)
