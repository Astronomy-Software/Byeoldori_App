package com.example.byeoldori.viewmodel.Community

import com.example.byeoldori.ui.components.community.EditorItem

data class EduProgram (
    val id: String,
    val title: String,
    val author: String,
    val profile: Int?,
    val rating: Float,
    val likeCount: Int,
    val commentCount: Int,
    val viewCount: Int, //조회수 순
    val createdAt: Long, //작성 시점
    val contentItems: List<EditorItem>,
)
