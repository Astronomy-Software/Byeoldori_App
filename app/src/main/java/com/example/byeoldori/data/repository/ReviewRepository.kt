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


private val Context.reviewLikedDataStore by preferencesDataStore(name = "review_liked_store")
object ReviewLikedPrefs {
    private val LIKED_REVIEW_KEYS = stringSetPreferencesKey("liked_review_keys")

    suspend fun saveLikedKeys(context: Context, keys: Set<String>) {
        context.reviewLikedDataStore.edit { prefs ->
            prefs[LIKED_REVIEW_KEYS] = keys
        }
    }

    suspend fun loadLikedKeys(context: Context): Set<String> {
        return context.reviewLikedDataStore.data
            .map { it[LIKED_REVIEW_KEYS] ?: emptySet() }
            .first()
    }
}

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
            size = 20,
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
        target: String,
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
                target = target,
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

    suspend fun saveLikedKeys(keys: Set<String>) {
        ReviewLikedPrefs.saveLikedKeys(context, keys)
    }

    suspend fun loadLikedKeys(): Set<String> {
        return ReviewLikedPrefs.loadLikedKeys(context)
    }
}