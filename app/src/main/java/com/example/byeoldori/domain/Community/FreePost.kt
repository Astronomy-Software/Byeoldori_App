package com.example.byeoldori.domain.Community

import com.example.byeoldori.domain.Content

data class FreePost(
    val id: String,
    val title: String,
    val author: String? = null,
    val profile: Int?,
    val likeCount: Int,
    //val content: String,
    val commentCount: Int,
    //val imageRes: Int,
    val viewCount: Int,
    val createdAt: String,
    val contentItems: List<Content>,
    val liked : Boolean,
    val thumbnail: String? = null
)