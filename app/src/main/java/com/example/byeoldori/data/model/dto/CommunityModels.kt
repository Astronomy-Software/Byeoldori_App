package com.example.byeoldori.data.model.dto

import com.squareup.moshi.JsonClass

enum class SortBy { LATEST, VIEWS, LIKES }
enum class SearchBy { TITLE, CONTENT, NICKNAME }

@JsonClass(generateAdapter = true)
data class CreateFreeRequest(
    val title: String,
    val content: String,
    val imageUrls: List<String>? = null
)

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