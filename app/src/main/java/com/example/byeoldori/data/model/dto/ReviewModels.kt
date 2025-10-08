package com.example.byeoldori.data.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateReviewRequest( // REVIEW 게시판용
    val title: String,
    val content: String,
    val review: ReviewDto, // Review 타입은 review 필드가 필수
    val imageUrls: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class ReviewDto(
    val location: String,
    val observationSiteId: Long? = null,
    val target: String,
    val equipment: String,
    val observationDate: String,
    val score: Int
)

@JsonClass(generateAdapter = true)
data class ReviewResponse(
    val id: Long,
    val type: String,
    val title: String,
    val authorId: Long,
    val authorNickname: String? = null,
    val contentSummary: String? = null,       // 본문 일부/요약
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class ReviewPostResponse(
    val content: List<ReviewResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)
