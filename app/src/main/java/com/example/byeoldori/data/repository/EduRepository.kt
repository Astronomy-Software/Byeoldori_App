package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.EducationApi
import com.example.byeoldori.data.model.dto.FeedbackRequest
import javax.inject.Inject

class EduRepository @Inject constructor(
    private val api: EducationApi
) {
    suspend fun submitFeedback(request: FeedbackRequest) =
        api.submitFeedback(request)

    suspend fun loadScenarioJson(url: String): String =
        api.loadScenarioJson(url).string()
}
