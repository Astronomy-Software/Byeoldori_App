package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.dto.*
import retrofit2.http.*

interface PlanApi {

    //단건 상세 조회
    @GET("calendar/events/{id}")
    suspend fun getPlanDetail(
        @Path("id") id: Long
    ): BaseResponse<PlanDetailDto>

    //월별 요약
    @GET("calendar/events/month")
    suspend fun getMonthlySummary(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): BaseResponse<List<MonthDaySummaryDto>>

    //관측 계획 작성
    @POST("calendar/events")
    suspend fun createPlan(
        @Body body: CreatePlanRequest
    ): BaseResponse<Long>

    @GET("calendar/events/date")
    suspend fun getEventsByDate(
        @Query("date") date: String // "YYYY-MM-DD"
    ): BaseResponse<List<PlanDetailDto>>

    @PATCH("calendar/events/{id}")
    suspend fun updatePlan(
        @Path("id") id: Long,
        @Body body: UpdatePlanRequest
    ): BaseResponse<PlanDetailDto>

    @DELETE("calendar/events/{id}")
    suspend fun deletePlan(
        @Path("id") id: Long
    ): BaseResponse<Unit>
}