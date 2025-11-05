package com.example.byeoldori.data.model.dto

import com.squareup.moshi.JsonClass

enum class EventStatus { PLANNED, COMPLETED, CANCELED }

@JsonClass(generateAdapter = true)
data class BaseResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T
)

@JsonClass(generateAdapter = true)
data class MonthDaySummaryDto(
    val date: String,     // "2025-11-05"
    val planned: Int,
    val completed: Int,
    val canceled: Int
)

@JsonClass(generateAdapter = true)
data class CreatePlanRequest(
    val title: String,
    val startAt: String,              // ISO-8601 (UTC 권장) e.g. "2025-11-05T11:28:17Z"
    val endAt: String,                // ISO-8601 (UTC 권장)
    val observationSiteId: Long? = null,
    val targets: List<String> = emptyList(),
    val lat: Double? = null,
    val lon: Double? = null,
    val placeName: String? = null,
    val memo: String? = null,
    val status: EventStatus = EventStatus.PLANNED,
    val imageUrls: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PlanDetailDto(
    val id: Long,
    val title: String,
    val startAt: String,     // 서버 예시: "2025-11-05T20:50"
    val endAt: String,
    val targets: List<String>,
    val observationSiteId: Long?,
    val observationSiteName: String?,
    val lat: Double?,
    val lon: Double?,
    val placeName: String?,
    val status: EventStatus,
    val memo: String?,
    val photos: List<PhotoDto>,
    val createdAt: String,
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class PhotoDto(
    val id: Long,
    val url: String,
    val contentType: String?
)

@JsonClass(generateAdapter = true)
data class UpdatePlanRequest(
    val title: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val targets: List<String>? = null,
    val observationSiteId: Long? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val placeName: String? = null,
    val memo: String? = null,
    val status: EventStatus? = null,
    val addImageUrls: List<String> = emptyList(),
    val removeImageIds: List<Long> = emptyList()
)