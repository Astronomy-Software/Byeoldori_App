package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.StarApi
import com.example.byeoldori.data.model.dto.ReviewResponse
import javax.inject.Inject

class StarRepository @Inject constructor(
    private val api: StarApi
) {
    suspend fun getReviewsByObject(objectName: String): List<ReviewResponse> {
        val res = api.getReviewsByObject(objectName)
        if (!res.success) throw IllegalStateException(res.message ?: "관측 후기 조회 실패")
        return res.data
    }
}