package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.dto.CommentResponse
import com.example.byeoldori.data.model.dto.CommentsPageResponse
import com.example.byeoldori.data.model.dto.CreateCommentRequest
import com.example.byeoldori.data.model.dto.CreateEducationRequest
import com.example.byeoldori.data.model.dto.CreateFreeRequest
import com.example.byeoldori.data.model.dto.CreateReviewRequest
import com.example.byeoldori.data.model.dto.CreatedPostId
import com.example.byeoldori.data.model.dto.EducationDetailResponse
import com.example.byeoldori.data.model.dto.EducationPostResponse
import com.example.byeoldori.data.model.dto.FreePostResponse
import com.example.byeoldori.data.model.dto.LikeToggleResponse
import com.example.byeoldori.data.model.dto.PostDetailResponse
import com.example.byeoldori.data.model.dto.PostResponse
import com.example.byeoldori.data.model.dto.ReviewDetailResponse
import com.example.byeoldori.data.model.dto.ReviewPostResponse
import com.example.byeoldori.data.model.dto.ReviewResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityApi {
    @GET("community/{type}/posts")
    suspend fun getPosts(
        @Path("type") type: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("sortBy") sortBy: String = "LATEST",
        @Query("keyword") keyword: String? = null,
        @Query("searchBy") searchBy: String = "TITLE"
    ): PostResponse

    @GET("community/REVIEW/posts")
    suspend fun getReviewPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("sortBy") sortBy: String = "LATEST",
        @Query("keyword") keyword: String? = null,
        @Query("searchBy") searchBy: String = "TITLE"
    ): ReviewPostResponse

    @GET("community/EDUCATION/posts")
    suspend fun getEducationPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("sortBy") sortBy: String = "LATEST",
        @Query("keyword") keyword: String? = null,
        @Query("searchBy") searchBy: String = "TITLE"
    ): EducationPostResponse

    @POST("community/FREE/posts")
    suspend fun createFreePost(@Body body: CreateFreeRequest): CreatedPostId

    @POST("community/REVIEW/posts")
    suspend fun createReviewPost(@Body body: CreateReviewRequest): CreatedPostId

    @POST("community/EDUCATION/posts")
    suspend fun createEducationPost(@Body body: CreateEducationRequest): CreatedPostId

    @GET("community/posts/{postId}")
    suspend fun getPostDetail(
        @Path("postId") postId: Long
    ): PostDetailResponse

    @GET("community/posts/{postId}")
    suspend fun getReviewDetail(
        @Path("postId") postId: Long
    ): ReviewDetailResponse

    @GET("community/posts/{postId}")
    suspend fun getEducationDetail(
        @Path("postId") postId: Long
    ): EducationDetailResponse

    @POST("community/posts/{id}/likes/toggle")
    suspend fun toggleLike(@Path("id") id: Long): LikeToggleResponse

    // 댓글 목록 조회 (페이징)
    @GET("community/posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: Long,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15
    ): CommentsPageResponse

    // 댓글/대댓글 작성
    @POST("community/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Long,
        @Body body: CreateCommentRequest
    ): CommentResponse

    @POST("community/posts/{postId}/comments/{commentId}/likes-toggle")
    suspend fun toggleCommentLike(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long
    ): LikeToggleResponse

    @DELETE("community/posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: Long
    )

    @DELETE("community/posts/{postId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long
    )


    @PATCH("community/posts/{postId}")
    suspend fun updateReview(
        @Path("postId") postId: Long,
        @Body body: CreateReviewRequest
    )

    @PATCH("community/posts/{postId}/comments/{commentId}")
    suspend fun updateComment(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long,
        @Body body: CreateCommentRequest
    ): CommentResponse

    @PATCH("community/posts/{postId}")
    suspend fun updatePost(
        @Path("postId") postId: Long,
        @Body body: CreateFreeRequest
    )
}