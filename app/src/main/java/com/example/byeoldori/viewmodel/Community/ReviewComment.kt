package com.example.byeoldori.viewmodel.Community

data class ReviewComment(
    val id: String,
    val reviewId: String, //어느 댓글에 달렸는지
    val author: String,
    val profile: Int?,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: Long
)
