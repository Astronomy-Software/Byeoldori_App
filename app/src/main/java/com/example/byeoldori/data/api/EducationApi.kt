package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.dto.FeedbackRequest
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface EducationApi {

    /**
     * ✅ 교육 피드백 저장 API
     * - programId, rating, good, bad 등을 서버에 보내 저장
     */
    @POST("education/feedback")
    suspend fun submitFeedback(
        @Body request: FeedbackRequest
    ): ResponseBody


    /**
     * ✅ 교육 프로그램 JSON 원격 로드
     * - @Url 사용해서 풀 경로를 직접 요청 가능
     *   ex) https://myserver.com/edu/test.json
     */
    @GET
    suspend fun loadScenarioJson(
        @Url url: String
    ): ResponseBody
}
