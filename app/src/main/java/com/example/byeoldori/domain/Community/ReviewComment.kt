package com.example.byeoldori.domain.Community

data class ReviewComment(
    val id: String,
    val reviewId: String, //어느 댓글에 달렸는지
    val parentId: String?, //대댓글인 경우 부모 댓글의 id
    val author: String,
    val profile: Int?,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: Long
)
