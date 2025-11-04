package com.example.byeoldori.viewmodel.Community

import androidx.lifecycle.*
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.data.repository.PlanRepository
import com.example.byeoldori.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repo: PlanRepository
) : ViewModel() {

    private val _monthSummaryState = MutableStateFlow<UiState<List<MonthDaySummaryDto>>>(UiState.Idle)
    val monthSummaryState: StateFlow<UiState<List<MonthDaySummaryDto>>> = _monthSummaryState

    private val _createState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val createState: StateFlow<UiState<Long>> = _createState

    private val _detailState = MutableStateFlow<UiState<PlanDetailDto>>(UiState.Idle)
    val detailState: StateFlow<UiState<PlanDetailDto>> = _detailState

    private val _monthPlansState = MutableStateFlow<UiState<List<PlanDetailDto>>>(UiState.Idle)
    val monthPlansState: StateFlow<UiState<List<PlanDetailDto>>> = _monthPlansState

    fun loadMonthSummary(year: Int, month: Int) {
        _monthSummaryState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val data = repo.getMonthlySummary(year, month)
                _monthSummaryState.value = UiState.Success(data)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                _monthSummaryState.value = UiState.Error(t.message ?: "월별 요약 조회 실패")
            }
        }
    }

    fun loadMonthPlans(year: Int, month: Int) {
        _monthPlansState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val data = repo.getMonthPlans(year, month)
                _monthPlansState.value = UiState.Success(data)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                _monthPlansState.value = UiState.Error(t.message ?: "월별 일정 조회 실패")
            }
        }
    }

    fun createPlan(body: CreatePlanRequest) {
        _createState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val data = repo.createPlan(body)
                _createState.value = UiState.Success(data)
            } catch (ce: CancellationException) {
                throw ce
            } catch(t: Throwable) {
                _createState.value = UiState.Error(t.message ?: "관측 계획 생성 실패")
            }
        }
    }

    fun loadPlanDetail(id: Long) {
        _detailState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val data = repo.getPlanDetail(id)
                _detailState.value = UiState.Success(data)
            } catch(ce: CancellationException) {
                throw ce
            } catch(t: Throwable) {
                _createState.value = UiState.Error(t.message ?: "관측 계획 상세 조회 실패")
            }
        }
    }

    /** 필요 시 외부에서 상태 초기화 */
    fun resetCreateState() { _createState.value = UiState.Idle }
    fun resetDetailState() { _detailState.value = UiState.Idle }
    fun resetMonthState() { _monthSummaryState.value = UiState.Idle }
}