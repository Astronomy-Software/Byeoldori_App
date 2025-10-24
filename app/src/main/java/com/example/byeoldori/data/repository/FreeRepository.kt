package com.example.byeoldori.data.repository

import android.content.ContentValues.TAG
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
import com.example.byeoldori.data.model.dto.PostDetailResponse
import com.example.byeoldori.data.model.dto.ReviewDto
import com.example.byeoldori.data.model.dto.SearchBy
import com.example.byeoldori.data.model.dto.SortBy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.likedDataStore by preferencesDataStore(name = "liked_store")
object LikedPrefs {
    private val LIKED_KEYS = stringSetPreferencesKey("liked_keys")

    suspend fun saveLikedKeys(context: Context, keys: Set<String>) {
        context.likedDataStore.edit { prefs ->
            prefs[LIKED_KEYS] = keys
        }
    }

    suspend fun loadLikedKeys(context: Context): Set<String> {
        return context.likedDataStore.data
            .map { it[LIKED_KEYS] ?: emptySet() }
            .first()
    }
}

class FreeRepository @Inject constructor(
    private val api: CommunityApi,
    @ApplicationContext private val context: Context
) {

    suspend fun getAllPosts(
        sortBy: SortBy = SortBy.LATEST,
        searchBy: SearchBy = SearchBy.TITLE,
        keyword: String? = null
    ): List<FreePostResponse> {
        return api.getPosts(
            type = "FREE",
            page = 0,
            size = 50,
            sortBy = sortBy.name,      // API에서 String 파라미터이므로 .name 유지
            searchBy = searchBy.name,
            keyword = keyword
        ).content
    }

    //상세 조회(id)
    suspend fun getPostDetail(postId: Long): PostDetailResponse {
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
    suspend fun saveLikedKeys(keys: Set<String>) {
        LikedPrefs.saveLikedKeys(context, keys)
    }

    suspend fun loadLikedKeys(): Set<String> {
        return LikedPrefs.loadLikedKeys(context)
    }

    suspend fun deletePost(postId: Long) {
        try {
            api.deletePost(postId)
            Log.d(TAG, "게시글 삭제 성공: id=$postId")
        } catch (e: Exception) {
            if (e is retrofit2.HttpException) {
                Log.e(TAG, "게시글 삭제 실패: code=${e.code()}, body=${e.response()?.errorBody()?.string()}")
            } else {
                Log.e(TAG, "게시글 삭제 실패: ${e.message}", e)
            }
            throw e
        }
    }

    suspend fun updatePost(
        postId: Long,
        title: String,
        content: String,
        imageUrls: List<String> = emptyList()
    ) {
        val imageFiltered = imageUrls.filter { it.startsWith("http://") || it.startsWith("https://") }
        val req = CreateFreeRequest(
            title = title,
            content = content,
            imageUrls = imageFiltered.ifEmpty { null }
        )
        api.updatePost(postId, req)
        Log.d(TAG, "리뷰 수정 완료 id=$postId")
    }
}