package com.example.byeoldori.ui.screen.Observatory

data class Review (
    val id: String,
    val title: String,
    val author: String,
    val rating: Float,
    val likeCount: Int,
    val commentCount: Int,
    val imageRes: Int
)