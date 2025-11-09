package com.example.byeoldori.viewmodel.Community

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.*
import com.example.byeoldori.data.local.datastore.PlanDataStore
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
    private val repo: PlanRepository,
    private val savedStateHandle: SavedStateHandle,
    private val planDataStore: PlanDataStore,
) : ViewModel() {

    private val _monthSummaryState = MutableStateFlow<UiState<List<MonthDaySummaryDto>>>(UiState.Idle)
    val monthSummaryState: StateFlow<UiState<List<MonthDaySummaryDto>>> = _monthSummaryState

    private val _createState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val createState: StateFlow<UiState<Long>> = _createState

    private val _detailState = MutableStateFlow<UiState<PlanDetailDto>>(UiState.Idle)
    val detailState: StateFlow<UiState<PlanDetailDto>> = _detailState

    private val _monthPlansState = MutableStateFlow<UiState<List<PlanDetailDto>>>(UiState.Idle)
    val monthPlansState: StateFlow<UiState<List<PlanDetailDto>>> = _monthPlansState

    private val _updateState = MutableStateFlow<UiState<PlanDetailDto>>(UiState.Idle)
    val updateState: StateFlow<UiState<PlanDetailDto>> = _updateState

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState

    private var lastPlans: List<PlanDetailDto> = emptyList()
    private val pendingDeleteIds = mutableSetOf<Long>() //삭제 진행 중인 ID를 기억
    private var monthReqSeq = 0

    private val _observationCount = MutableStateFlow<Int>(0)
    val observationCount: StateFlow<Int> = _observationCount

    private val alarmMinutesCache = mutableStateMapOf<Long, Int>()

    fun alarmMinutesOf(planId: Long): Flow<Int> =
        planDataStore.minutesOf(planId)
            .onEach { minutes -> alarmMinutesCache[planId] = minutes }

    fun getAlarmMinutes(planId: Long): Int = alarmMinutesCache[planId] ?: 60
    fun setAlarmMinutes(planId: Long, minutes: Int) {
        // 즉시 캐시 반영 (UI 지연 최소화)
        alarmMinutesCache[planId] = minutes
        viewModelScope.launch {
            planDataStore.setMinutesOf(planId, minutes)
        }
    }

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
        viewModelScope.launch {
            _monthPlansState.value = UiState.Loading
            runCatching { repo.getMonthPlans(year, month) }
                .onSuccess { data ->
                    lastPlans = data
                    // ❶ 항상 pendingDeleteIds로 필터링
                    val filtered = data.filterNot { it.id in pendingDeleteIds }
                    _monthPlansState.value = UiState.Success(filtered)

                    // ❷ 서버 목록에서 완전히 사라진 id는 pending에서 정리
                    val dataIds = data.asSequence().map { it.id }.toSet()
                    pendingDeleteIds.retainAll(dataIds) // 사라진 것들은 제거됨
                }
                .onFailure { e ->
                    _monthPlansState.value = UiState.Error(e.message ?: "목록 조회 실패")
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
                _detailState.value = UiState.Error(t.message ?: "관측 계획 상세 조회 실패")
            }
        }
    }

    fun updatePlan(id: Long, body: UpdatePlanRequest) {
        _updateState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val updated = repo.updatePlan(id, body)
                _updateState.value = UiState.Success(updated)
                _detailState.value = UiState.Success(updated)

                // lastPlans 교체
                lastPlans = lastPlans.map { if (it.id == updated.id) updated else it }

                //항상 pendingDeleteIds로 필터된 리스트만 화면에 내보냄
                _monthPlansState.value = UiState.Success(
                    lastPlans.filterNot { it.id in pendingDeleteIds }
                )
            } catch (t: Throwable) {
                _updateState.value = UiState.Error(t.message ?: "관측 계획 수정 실패")
            }
        }
    }

    fun deletePlan(id: Long, year: Int, month: Int) {
        pendingDeleteIds += id

        //lastPlans도 함께 필터링해서 고정
        lastPlans = lastPlans.filterNot { it.id == id }

        // 화면에 즉시 반영(항상 pendingDeleteIds로 걸러진 결과만)
        _monthPlansState.value = UiState.Success(
            lastPlans.filterNot { it.id in pendingDeleteIds }
        )

        viewModelScope.launch {
            runCatching { repo.deletePlan(id) }
                .onSuccess {
                    loadMonthPlans(year, month) // 재조회 결과도 VM 내부에서 다시 필터됨
                }
                .onFailure {
                    // 롤백: 실패면 pending에서 빼고, 원본 복구(여기도 필터 일관성 유지)
                    pendingDeleteIds -= id
                    _monthPlansState.value = UiState.Success(
                        lastPlans.filterNot { it.id in pendingDeleteIds }
                    )
                }
        }
    }
    fun loadObservationCount() {
        viewModelScope.launch {
            runCatching { repo.getObservationCount() }
                .onSuccess { _observationCount.value = it }
                .onFailure { /* 필요하면 로그만 */ }
        }
    }

    fun completePlan(id: Long, observedAt: java.time.OffsetDateTime? = null) {
        viewModelScope.launch {
            runCatching {
                repo.completeEvent(
                    id,
                    observedAt?.toString() // ISO-8601 "2025-11-07T15:47:32Z"
                )
            }.onSuccess { updated ->
                // 목록/상태 갱신
                lastPlans = lastPlans.map { if (it.id == updated.id) updated else it }
                _monthPlansState.value = UiState.Success(lastPlans.filterNot { it.id in pendingDeleteIds })
                // 완료되었으니 카운트도 갱신
                loadObservationCount()
            }.onFailure {
                // TODO: 에러 처리(스낵바/로그)
            }
        }
    }

    /** 필요 시 외부에서 상태 초기화 */
    fun resetCreateState() { _createState.value = UiState.Idle }
    fun resetDetailState() { _detailState.value = UiState.Idle }
    fun resetMonthState() { _monthSummaryState.value = UiState.Idle }
    fun resetUpdateState() { _updateState.value = UiState.Idle }
    fun resetDeleteState() { _deleteState.value = UiState.Idle }
}