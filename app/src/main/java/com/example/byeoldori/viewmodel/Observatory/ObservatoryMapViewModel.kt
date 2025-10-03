package com.example.byeoldori.viewmodel.Observatory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.ObservationSite
import com.example.byeoldori.data.repository.ObservationSiteRepository
import com.example.byeoldori.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ObservatoryMapViewModel @Inject constructor(
    private val repo: ObservationSiteRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<ObservationSite>>>(UiState.Idle)
    val state: StateFlow<UiState<List<ObservationSite>>> = _state

    init {
        loadSites()
    }

    fun loadSites() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val sites = repo.getAllSites()
                _state.value = UiState.Success(sites)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "관측지 정보를 불러오지 못했습니다.")
            }
        }
    }
}