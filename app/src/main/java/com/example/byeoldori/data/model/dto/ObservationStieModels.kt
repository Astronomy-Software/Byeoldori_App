package com.example.byeoldori.data.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ObservationSite(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

@JsonClass(generateAdapter = true)
data class ObservationSitesRecommendRequest(
    @Json(name = "userLat") val userLat: Double,
    @Json(name = "userLon") val userLon: Double,
    @Json(name = "observationTime") val observationTime: String,
)

@JsonClass(generateAdapter = true)
data class ObservationSiteRegisterRequest(
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)

@JsonClass(generateAdapter = true)
data class ObservationSiteUpdateRequest(
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)