package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.dto.CreateEducationRequest
import com.example.byeoldori.data.model.dto.CreateFreeRequest
import com.example.byeoldori.data.model.dto.CreateReviewRequest
import com.example.byeoldori.data.model.dto.CreatedPostId
import com.example.byeoldori.data.model.dto.FreePostResponse
import com.example.byeoldori.data.model.dto.LikeToggleResponse
import com.example.byeoldori.data.model.dto.PostResponse
import com.example.byeoldori.data.model.dto.ReviewPostResponse
import com.example.byeoldori.data.model.dto.ReviewResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityApi {
    @GET("community/{type}/posts")
    suspend fun getPosts(
        @Path("type") type: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "LATEST",
        @Query("keyword") keyword: String? = null,
        @Query("searchBy") searchBy: String = "TITLE"
    ): PostResponse

    @GET("community/REVIEW/posts")
    suspend fun getReviewPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "LATEST",
        @Query("keyword") keyword: String? = null,
        @Query("searchBy") searchBy: String = "TITLE"
    ): ReviewPostResponse

    @POST("community/FREE/posts")
    suspend fun createFreePost(@Body body: CreateFreeRequest): CreatedPostId

    @POST("community/REVIEW/posts")
    suspend fun createReviewPost(@Body body: CreateReviewRequest): CreatedPostId

    @POST("community/EDUCATION/posts")
    suspend fun createEducationPost(@Body body: CreateEducationRequest): CreatedPostId

    @GET("community/posts/{postId}")
    suspend fun getPostDetail(
        @Path("postId") postId: Long
    ): FreePostResponse

    @GET("community/posts/{postId}")
    suspend fun getReviewDetail(
        @Path("postId") postId: Long
    ): ReviewResponse

    @POST("community/posts/{id}/likes/toggle")
    suspend fun toggleLike(@Path("id") id: Long): LikeToggleResponse
}