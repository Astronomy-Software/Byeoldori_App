package com.example.byeoldori.data.model.dto

import com.squareup.moshi.JsonClass

enum class CommunityType { FREE, REVIEW, EDUCATION }
enum class SortBy { LATEST, VIEWS, LIKES }
enum class SearchBy { TITLE, CONTENT, NICKNAME }

@JsonClass(generateAdapter = true)
data class CreatePostRequest(
    val title: String,
    val content: String,
    val review: ReviewDto? = null,
    val education: EducationDto? = null,
    val imageUrls: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class CreateFreeRequest(
    val title: String,
    val content: String,
    val imageUrls: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class CreateReviewRequest( // REVIEW 게시판용
    val title: String,
    val content: String,
    val review: ReviewDto, // Review 타입은 review 필드가 필수
    val imageUrls: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class CreateEducationRequest(
    val title: String,
    val content: String,
    val education: EducationDto,
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
data class EducationDto(
    val summary: String,
    val difficulty: Difficulty,
    val tags: String,
    val status: EduStatus
)

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }
enum class EduStatus { DRAFT, PUBLISHED }


@JsonClass(generateAdapter = true)
data class FreePostResponse(
    val id: Long,
    val type: String,
    val title: String,
    val authorId: Long,
    val authorNickname: String? = null,
    val contentSummary: String,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class CreatedPostId(val id: Long)

@JsonClass(generateAdapter = true)
data class LikeToggleResponse(
    val liked: Boolean,
    val likes: Long
)

@JsonClass(generateAdapter = true)
data class PostResponse(
    val content: List<FreePostResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)