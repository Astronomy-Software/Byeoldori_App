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
    val liked: Boolean
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

@JsonClass(generateAdapter = true)
data class PostDetailResponse(
    val id: Long,
    val type: String,
    val title: String,
    val content: String,
    val authorId: Long,
    val images: List<String> = emptyList(),
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val updatedAt: String? = null,
    val liked: Boolean
)


@JsonClass(generateAdapter = true)
data class CreateCommentRequest(
    val content: String,
    val parentId: Long? = null   // 대댓글이면 부모 ID, 일반 댓글이면 null
)

@JsonClass(generateAdapter = true)
data class CommentResponse(
    val id: Long,
    val authorId: Long,
    val authorNickname: String? = null,
    val content: String,
    val createdAt: String,
    val parentId: Long?,
    val depth: Int,
    val deleted: Boolean,
    val likeCount: Int,
    val liked: Boolean
)

@JsonClass(generateAdapter = true)
data class CommentsPageResponse(
    val content: List<CommentResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)