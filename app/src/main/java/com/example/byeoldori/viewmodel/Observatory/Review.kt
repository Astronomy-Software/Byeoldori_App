package com.example.byeoldori.viewmodel.Observatory

data class Review (
    val id: String,
    val title: String,
    val author: String,
    val rating: Float,
    val likeCount: Int,
    val commentCount: Int,
    val imageRes: Int
)