package com.example.byeoldori.data.repository

import android.util.Log
import com.example.byeoldori.data.api.CommunityApi
import com.example.byeoldori.data.model.dto.CommunityType
import com.example.byeoldori.data.model.dto.CreateFreeRequest
import com.example.byeoldori.data.model.dto.CreatePostRequest
import com.example.byeoldori.data.model.dto.Difficulty
import com.example.byeoldori.data.model.dto.EduStatus
import com.example.byeoldori.data.model.dto.EducationDto
import com.example.byeoldori.data.model.dto.FreePostResponse
import com.example.byeoldori.data.model.dto.LikeToggleResponse
import com.example.byeoldori.data.model.dto.ReviewDto
import com.example.byeoldori.data.model.dto.SearchBy
import com.example.byeoldori.data.model.dto.SortBy
import com.example.byeoldori.ui.components.community.freeboard.FreeBoardSort
import javax.inject.Inject

class CommunityRepository @Inject constructor(
    private val api: CommunityApi,
) {

    suspend fun getAllPosts(
        sortBy: SortBy = SortBy.LATEST,
        searchBy: SearchBy = SearchBy.TITLE,
        keyword: String? = null
    ): List<FreePostResponse> {
        return api.getPosts(
            type = "FREE",
            page = 0,
            size = 20,
            sortBy = sortBy.name,      // API에서 String 파라미터이므로 .name 유지
            searchBy = searchBy.name,
            keyword = keyword
        ).content
    }

    //상세 조회(id)
    suspend fun getPostDetail(postId: Long): FreePostResponse {
        return api.getPostDetail(postId)
    }

    suspend fun createFreePost(
        title: String,
        content: String,
        imageUrls: List<String> = emptyList()
    ): Long {
        // 서버가 접근 가능한 URL만 통과 (content:// 등 로컬 URI는 제외)
        val sanitized = imageUrls.filter { it.startsWith("http://") || it.startsWith("https://") }

        val req = CreateFreeRequest(
            title = title,
            content = content,
            imageUrls = sanitized.ifEmpty { null } // 서버가 null 허용이면 null, 아니면 빈배열 []
        )

        val res = api.createFreePost(req)
        Log.d("CommunityRepo", "게시글 생성 완료 id=${res.id}")
        return res.id
    }

    suspend fun toggleLike(postId: Long): LikeToggleResponse {
        return try {
            val res = api.toggleLike(postId)
            Log.d("CommunityRepo", "좋아요 토글 성공: liked=${res.liked}, likes=${res.likes}")
            res
        } catch (e: Exception) {
            if (e is retrofit2.HttpException) {
                val code = e.code()
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("CommunityRepo", "좋아요 토글 실패: code=$code, body=$errorBody")
            } else {
                Log.e("CommunityRepo", "좋아요 토글 실패: ${e.message}", e)
            }
            throw e
        }
    }
}