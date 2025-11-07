package com.example.byeoldori.data.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateEducationRequest(
    val title: String,
    val content: String,
    val education: EducationDto,
    val imageUrls: List<String>? = null
)

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }
enum class EduStatus { DRAFT, PUBLISHED }

@JsonClass(generateAdapter = true)
data class EducationDto(
    val summary: String? = null,
    val difficulty: Difficulty? = null,
    val tags: String? = null,
    val status: EduStatus? = null,
    val target: String? = null,
    val averageScore: Double? = 0.0
)


@JsonClass(generateAdapter = true)
data class EducationResponse(
    val id: Long,
    val type: String,
    val title: String,
    val authorId: Long,
    val authorNickname: String? = null,
    val contentSummary: String? = null,       // 본문 일부/요약
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val liked: Boolean
)

@JsonClass(generateAdapter = true)
data class EducationPostResponse(
    val content: List<EducationResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)

@JsonClass(generateAdapter = true)
data class EducationDetailResponse(
    val id: Long,
    val type: String,
    val title: String,
    val content: String,
    val authorId: Long,
    val images: List<String> = emptyList(),
    val education: EducationDto?,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val liked: Boolean
)

data class FeedbackRequest(
    val programId: String,
    val rating: Int,
    val good: String,
    val bad: String
)