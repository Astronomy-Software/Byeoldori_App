package com.example.byeoldori.domain.Community

import com.example.byeoldori.domain.Content

data class EduProgram (
    val id: String,
    val title: String,
    val author: String,
    val authorId: Long,
    val authorProfileImageUrl: String?,
    val profile: Int?,
    val rating: Float,
    val likeCount: Int,
    val commentCount: Int,
    val viewCount: Int, //조회수 순
    val createdAt: String, //작성 시점
    val contentItems: List<Content>,
    val liked: Boolean,
    val targets: List<String>? = null,
    val averageScore: Double? = 0.0,
    val thumbnail: String? = null
)
