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
import com.example.byeoldori.data.model.dto.LikeToggleResponse
import com.example.byeoldori.data.model.dto.SearchBy
import com.example.byeoldori.data.model.dto.SortBy
import com.example.byeoldori.utils.SweObjUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class EducationRepository @Inject constructor(
    private val api: CommunityApi,
    @ApplicationContext private val context: Context
) {
    private val TAG = "EducationRepository"

    private fun List<String>?.toSwe(): List<String>? =
        this?.map { SweObjUtils.toSweFormat(it) }

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
        return api.getEducationDetail(postId)
    }

    // 게시글 생성
    suspend fun createEducationPost(
        title: String,
        content: String,
        summary: String,
        difficulty: Difficulty,
        tags: String,
        status: EduStatus,
        imageUrls: List<String> = emptyList(),
        targets: List<String>? = null,
    ): Long {
        val sanitized = imageUrls.filter { it.startsWith("http://") || it.startsWith("https://") }
        val targetsSwe: List<String>? = targets.toSwe()

        val req = CreateEducationRequest(
            title = title,
            content = content,
            education = EducationDto(
                summary = summary,
                difficulty = difficulty,
                tags = tags,
                status = status,
                targets = targetsSwe ?: emptyList(),
                averageScore = 0.0
            ),
            imageUrls = sanitized.ifEmpty { null }
        )

        val res = api.createEducationPost(req)
        Log.d(TAG, "교육 게시글 생성 완료(dto) id=${res.id}")
        return res.id
    }

    suspend fun toggleLike(postId: Long): LikeToggleResponse {
        return try {
            val res = api.toggleLike(postId)
            Log.d(TAG, "좋아요 토글 성공: liked=${res.liked}, likes=${res.likes}")
            res
        } catch (e: retrofit2.HttpException) {
            val code = e.code()
            val body = e.response()?.errorBody()?.string()
            Log.e(TAG, "좋아요 토글 실패 (HTTP): code=$code, body=$body", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "좋아요 토글 실패 (예외): ${e.message}", e)
            throw e
        }
    }

}