package com.example.byeoldori.viewmodel.Community

data class FreePost(
    val id: String,
    val title: String,
    val author: String,
    val likeCount: Int,
    val content: String,
    val commentCount: Int,
    val imageRes: Int,
    val viewCount: Int,
    val createdAt: Long
)
