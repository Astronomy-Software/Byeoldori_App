package com.example.byeoldori.domain.Community

import com.example.byeoldori.domain.Content

data class FreePost(
    val id: String,
    val title: String,
    val author: String,
    val profile: Int?,
    val likeCount: Int,
    //val content: String,
    val commentCount: Int,
    //val imageRes: Int,
    val viewCount: Int,
    val createdAt: Long,
    val contentItems: List<Content>
)
