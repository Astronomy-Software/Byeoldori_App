package com.example.byeoldori.data.repository

import android.util.Log
import com.example.byeoldori.data.api.CommunityApi
import com.example.byeoldori.data.model.dto.CommunityType
import com.example.byeoldori.data.model.dto.Post
import javax.inject.Inject

class CommunityRepository @Inject constructor(
    private val api: CommunityApi,
) {

    suspend fun getAllPosts(type: CommunityType): List<Post> = try {
        val typeParam = when (type) {
            CommunityType.FREE -> "FREE"
            CommunityType.REVIEW -> "REVIEW"
            CommunityType.EDUCATION -> "EDUCATION"
        }
        api.getPosts(typeParam, page = 0, size = 20).content
    } catch (e: Exception) {
        Log.e("CommunityRepository", "getAllPosts failed", e)
        throw e
    }

    //상세 조회(id)
    suspend fun getPostDetail(postId: Long): Post {
        return api.getPostDetail(postId)
    }
}