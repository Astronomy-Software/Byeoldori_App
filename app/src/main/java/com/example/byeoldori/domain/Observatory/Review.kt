package com.example.byeoldori.domain.Observatory

import com.example.byeoldori.domain.Content
import com.example.byeoldori.ui.components.community.EditorItem

data class Review (
    val id: String,
    val title: String,
    val author: String,
    val rating: Int, //관측 평점
    val likeCount: Int,
    //val imageRes: Int?,
    val commentCount: Int,
    val profile: Int?,
    val viewCount: Int,  //조회수
    val createdAt: String, //작성 시점
    val target: String,  //관측 대상
    val site: String,    //관측지
    val date: String,    //관측 일자
    val siteScore: Int,
    val equipment: String,
    val startTime: String,
    val endTime: String,
    val contentItems: List<Content>, //본문 + 이미지
)