package com.example.byeoldori.data.model.dto

import com.google.gson.annotations.SerializedName
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
    val imageUrls: List<String> = emptyList()
)

data class ReviewDto(
    val location: String,
    val target: String,
    val equipment: String,
    val observationDate: String,
    val score: Int
)

data class EducationDto(
    val summary: String,
    val difficulty: Difficulty,
    val tags: String,
    val status: EduStatus
)

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }
enum class EduStatus { DRAFT, PUBLISHED }

@JsonClass(generateAdapter = true)
data class PostResponse(
    val content: List<Post>,
    val totalPages: Int? = null,
    val totalElements: Long? = null,
    val size: Int? = null,
    val number: Int? = null,
    val first: Boolean? = null,
    val last: Boolean? = null,
    val empty: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class Post(
    val id: Long,
    val type: String,
    val title: String,
    val authorId: Long,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String

    //이 부분은 상세조회했을 때
//    val content: String? = null,
//    val images: List<String>? = null,
//    val review: ReviewDto? = null,
//    val education: EducationDto? = null,
//    val updatedAt: String? = null
)

data class CreatedPostId(val id: Long)