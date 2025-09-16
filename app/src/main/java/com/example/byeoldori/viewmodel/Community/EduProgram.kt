package com.example.byeoldori.viewmodel.Community

data class EduProgram (
    val id: String,
    val title: String,
    val author: String,
    val rating: Float,
    val likeCount: Int,
    val commentCount: Int,
    val imageRes: Int,
    val viewCount: Int, //조회수 순
    val createdAt: Long //작성 시점
)
