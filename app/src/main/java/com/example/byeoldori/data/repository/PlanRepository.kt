package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.PlanApi
import com.example.byeoldori.data.model.dto.*
import kotlinx.coroutines.async
import kotlinx.coroutines.*
import javax.inject.Inject

class PlanRepository @Inject constructor(
    private val api: PlanApi
) {
    suspend fun createPlan(body: CreatePlanRequest): Long {
        val response = api.createPlan(body)
        if (!response.success) throw IllegalStateException(response.message ?: "관측 계획 생성 실패")
        return response.data
    }

    suspend fun getPlanDetail(id: Long): PlanDetailDto {
        val response = api.getPlanDetail(id)
        if (!response.success) throw IllegalStateException(response.message ?: "관측 계획 상세 조회 실패")
        return response.data
    }

    suspend fun getMonthlySummary(year: Int, month: Int): List<MonthDaySummaryDto> {
        val response = api.getMonthlySummary(year, month)
        if (!response.success) throw IllegalStateException(response.message ?: "월별 요약 조회 실패")
        return response.data
    }

    suspend fun getMonthPlans(year: Int, month: Int): List<PlanDetailDto> = coroutineScope {
        val summary = getMonthlySummary(year, month)

        val dates = summary.filter { it.planned + it.completed + it.canceled > 0 }.map { it.date }

        val perDay = dates.map { d ->
            async {
                val response = api.getEventsByDate(d)
                if (!response.success) throw IllegalStateException(response.message ?: "일별 일정 조회 실패")
                response.data
            }
        }.awaitAll()

        perDay.flatten().sortedBy { it.startAt }
    }

    suspend fun updatePlan(id: Long, body: UpdatePlanRequest): PlanDetailDto {
        val response = api.updatePlan(id, body)
        if (!response.success) throw IllegalStateException(response.message ?: "관측 계획 수정 실패")
        return response.data
    }

    suspend fun deletePlan(id: Long) {
        val response = api.deletePlan(id)
        if(!response.success) throw IllegalStateException(response.message ?: "관측 계획 삭제 실패")
    }
}