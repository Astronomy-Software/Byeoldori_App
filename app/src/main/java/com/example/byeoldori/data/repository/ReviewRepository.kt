package com.example.byeoldori.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.byeoldori.data.api.CommunityApi
import com.example.byeoldori.data.model.dto.CreateFreeRequest
import com.example.byeoldori.data.model.dto.CreateReviewRequest
import com.example.byeoldori.data.model.dto.FreePostResponse
import com.example.byeoldori.data.model.dto.LikeToggleResponse
import com.example.byeoldori.data.model.dto.ReviewDetailResponse
import com.example.byeoldori.data.model.dto.ReviewDto
import com.example.byeoldori.data.model.dto.ReviewPostResponse
import com.example.byeoldori.data.model.dto.ReviewResponse
import com.example.byeoldori.data.model.dto.SearchBy
import com.example.byeoldori.data.model.dto.SortBy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val api: CommunityApi,
    @ApplicationContext private val context: Context
) {
    private val TAG = "ReviewRepository"

    suspend fun getAllReviews(
        sortBy: SortBy = SortBy.LATEST,
        searchBy: SearchBy = SearchBy.TITLE,
        keyword: String? = null
    ): List<ReviewResponse> {
        val res = api.getReviewPosts(
            page = 0,
            size = 50,
            sortBy = sortBy.name,
            searchBy = searchBy.name,
            keyword = keyword
        )
        return res.content
    }

    suspend fun getReviewDetail(postId: Long): ReviewDetailResponse {
        return api.getReviewDetail(postId)
    }

    suspend fun createReviewPost(
        title: String,
        content: String,
        location: String,
        targets: List<String>,
        equipment: String,
        observationDate: String,
        score: Int,
        observationSiteId: Long? = null,
        imageUrls: List<String> = emptyList()
    ): Long {
        // 서버가 접근 가능한 URL만 통과 (content:// 등 로컬 URI는 제외)
        val sanitized = imageUrls.filter { it.startsWith("http://") || it.startsWith("https://") }

        val req = CreateReviewRequest(
            title = title,
            content = content,
            review = ReviewDto(
                location = location,
                observationSiteId = observationSiteId,
                targets = targets,
                equipment = equipment,
                observationDate = observationDate,
                score = score
            ),
            imageUrls = sanitized.ifEmpty { null } // 서버가 null 허용이면 null, 아니면 빈배열 []
        )

        val res = api.createReviewPost(req)
        Log.d(TAG, "게시글 생성 완료(dto) id=${res.id}")
        return res.id
    }

    suspend fun toggleLike(postId: Long): LikeToggleResponse {
        return try {
            val res = api.toggleLike(postId)
            Log.d(TAG, "좋아요 토글 성공: liked=${res.liked}, likes=${res.likes}")
            res
        } catch (e: Exception) {
            if (e is retrofit2.HttpException) {
                val code = e.code()
                val body = e.response()?.errorBody()?.string()
                Log.e(TAG, "좋아요 토글 실패: code=$code, body=$body")
            } else {
                Log.e(TAG, "좋아요 토글 실패: ${e.message}", e)
            }
            throw e
        }
    }

    //상세조회에서의 좋아요
    fun applyLikeToDetail(
        detail: ReviewDetailResponse,
        liked: Boolean,
        likeCount: Int
    ): ReviewDetailResponse = detail.copy(liked = liked, likeCount = likeCount)

    //관측지에서의 관측지 정보
    data class SiteInfo (
        val reviewCount: Int,
        val likeCount: Int,
        val avgRating: Float
    )

    suspend fun getReviewBySite(siteId: Long): List<ReviewResponse> {
        val all = getAllReviews(sortBy = SortBy.LATEST)
        return all.filter { it.observationSiteId == siteId }
    }

    //관측지 리뷰의 리뷰수, 좋아요수, 평점 수 계산
    suspend fun getSiteInfo(siteId: Long) : SiteInfo {
        val reviews = getReviewBySite(siteId)
        val count = reviews.size
        val likeSum = reviews.sumOf { it.likeCount }
        val avgRating =
            if(count > 0) reviews.mapNotNull { it.score }.average().toFloat()
            else 0f
        return SiteInfo(count, likeSum, avgRating)
    }

    suspend fun updateReview(
        postId: Long,
        title: String,
        content: String,
        location: String,
        targets: List<String>,
        equipment: String,
        observationDate: String,
        score: Int,
        observationSiteId: Long? = null,
        imageUrls: List<String> = emptyList()
    ) {
        val imageFiltered = imageUrls.filter { it.startsWith("http://") || it.startsWith("https://") }
        val req = CreateReviewRequest(
            title = title,
            content = content,
            review = ReviewDto(
                location = location,
                observationSiteId = observationSiteId,
                targets = targets,
                equipment = equipment,
                observationDate = observationDate,
                score = score
            ),
            imageUrls = imageFiltered.ifEmpty { null }
        )
        api.updateReview(postId, req)
        Log.d(TAG, "리뷰 수정 완료 id=$postId")
    }
}