package com.example.byeoldori.viewmodel.Community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.EducationResponse
import com.example.byeoldori.data.model.dto.ReviewResponse
import com.example.byeoldori.data.repository.StarRepository
import com.example.byeoldori.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StarViewModel @Inject constructor(
    private val starRepository: StarRepository
) : ViewModel() {

    private val _reviewsState = MutableStateFlow<UiState<List<ReviewResponse>>>(UiState.Idle)
    val reviewsState: StateFlow<UiState<List<ReviewResponse>>> = _reviewsState

    private val _programsState = MutableStateFlow<UiState<List<EducationResponse>>>(UiState.Idle)
    val programsState: StateFlow<UiState<List<EducationResponse>>> = _programsState

    fun loadObjectReviews(objectName: String) = viewModelScope.launch {
        _reviewsState.value = UiState.Loading
        runCatching { starRepository.getReviewsByObject(objectName) }
            .onSuccess { _reviewsState.value = UiState.Success(it) }
            .onFailure { _reviewsState.value = UiState.Error(it.message ?: "관측 후기 조회 실패") }
    }

    fun loadObjectPrograms(objectName: String) = viewModelScope.launch {
        _programsState.value = UiState.Loading
        runCatching { starRepository.getProgramsByObject(objectName) }
            .onSuccess { _programsState.value = UiState.Success(it) }
            .onFailure { _programsState.value = UiState.Error(it.message ?: "교육 프로그램 조회 실패") }
    }
}