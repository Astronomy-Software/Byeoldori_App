package com.example.byeoldori.viewmodel.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.FindEmailRequset
import com.example.byeoldori.data.repository.FindAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ✅ 화면 전용 UI State
sealed class FindEmailUiState {
    object Idle : FindEmailUiState()
    object Loading : FindEmailUiState()
    data class Success(val email: String) : FindEmailUiState()
    data class Error(val message: String) : FindEmailUiState()
}

@HiltViewModel
class FindEmailViewModel @Inject constructor(
    private val repo: FindAccountRepository
) : ViewModel() {

    // ✅ FindEmail 전용 상태
    private val _state = MutableStateFlow<FindEmailUiState>(FindEmailUiState.Idle)
    val state: StateFlow<FindEmailUiState> = _state

    fun findEmail(name: String, phone: String) = viewModelScope.launch {
        _state.value = FindEmailUiState.Loading
        try {
            val req = FindEmailRequset(name = name, phone = phone)
            val resp = repo.findEmail(req)

            if (resp.success) {
                val email = resp.data ?: ""
                _state.value = FindEmailUiState.Success(email)
            } else {
                _state.value = FindEmailUiState.Error(resp.message ?: "이메일 찾기 실패")
            }
        } catch (e: Exception) {
            _state.value = FindEmailUiState.Error("네트워크 오류: ${e.message}")
        }
    }
}
