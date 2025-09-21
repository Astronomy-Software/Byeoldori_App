package com.example.byeoldori.viewmodel.Community

import com.example.byeoldori.ui.components.community.EditorItem

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
    val contentItems: List<EditorItem>,

)
