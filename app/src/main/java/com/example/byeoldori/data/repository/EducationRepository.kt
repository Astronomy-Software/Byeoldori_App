package com.example.byeoldori.data.repository

import android.content.Context
import android.util.Log
import com.example.byeoldori.data.api.CommunityApi
import com.example.byeoldori.data.model.dto.CreateEducationRequest
import com.example.byeoldori.data.model.dto.Difficulty
import com.example.byeoldori.data.model.dto.EduStatus
import com.example.byeoldori.data.model.dto.EducationDetailResponse
import com.example.byeoldori.data.model.dto.EducationDto
import com.example.byeoldori.data.model.dto.EducationPostResponse
import com.example.byeoldori.data.model.dto.EducationResponse
import com.example.byeoldori.data.model.dto.SearchBy
import com.example.byeoldori.data.model.dto.SortBy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class EducationRepository @Inject constructor(
    private val api: CommunityApi,
    @ApplicationContext private val context: Context
) {
    private val TAG = "EducationRepository"

    // 목록 불러오기
    suspend fun getAllEducations(
        sortBy: SortBy = SortBy.LATEST,
        searchBy: SearchBy = SearchBy.TITLE,
        keyword: String? = null
    ): List<EducationResponse> {
        val res = api.getEducationPosts(
            page = 0,
            size = 20,
            sortBy = sortBy.name,
            searchBy = searchBy.name,
            keyword = keyword
        )
        return res.content
    }

    // 상세 불러오기
    suspend fun getEducationDetail(postId: Long): EducationDetailResponse {
        return api.getEducationDetail(postId) // ← 서버 구조상 공통 엔드포인트 사용
    }

    // 게시글 생성
    suspend fun createEducationPost(
        title: String,
        content: String,
        summary: String,
        difficulty: Difficulty,
        tags: String,
        status: EduStatus,
        imageUrls: List<String> = emptyList()
    ): Long {
        val sanitized = imageUrls.filter { it.startsWith("http://") || it.startsWith("https://") }

        val req = CreateEducationRequest(
            title = title,
            content = content,
            education = EducationDto(
                summary = summary,
                difficulty = difficulty,
                tags = tags,
                status = status
            ),
            imageUrls = sanitized.ifEmpty { null }
        )

        val res = api.createEducationPost(req)
        Log.d(TAG, "교육 게시글 생성 완료(dto) id=${res.id}")
        return res.id
    }
}