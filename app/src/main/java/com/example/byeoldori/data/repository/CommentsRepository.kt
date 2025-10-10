package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.CommunityApi
import com.example.byeoldori.data.model.dto.CommentResponse
import com.example.byeoldori.data.model.dto.CommentsPageResponse
import com.example.byeoldori.data.model.dto.CreateCommentRequest
import com.example.byeoldori.data.model.dto.LikeToggleResponse
import javax.inject.Inject

class CommentsRepository @Inject constructor(
    private val api: CommunityApi
) {
    suspend fun getComments(
        postId: Long,
        page: Int = 1,
        size: Int = 15
    ): Result<CommentsPageResponse> = runCatching {
        api.getComments(postId, page, size)
    }

    suspend fun createComment(
        postId: Long,
        content: String,
        parentId: Long? = null
    ): Result<CommentResponse> = runCatching {
        api.createComment(postId, CreateCommentRequest(content = content, parentId = parentId))
    }

    suspend fun toggleCommentLike(postId: Long, commentId: Long): LikeToggleResponse {
        return api.toggleCommentLike(postId, commentId)
    }
}
