package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.dto.BaseResponse
import com.example.byeoldori.data.model.dto.ReviewResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface StarApi {
    @GET("stars/{objectName}/reviews")
    suspend fun getReviewsByObject(
        @Path("objectName") objectName: String,
    ):BaseResponse<List<ReviewResponse>>
}