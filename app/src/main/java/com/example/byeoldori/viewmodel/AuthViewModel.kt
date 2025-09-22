package com.example.byeoldori.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI 상태
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    // 화면 상태
    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state.asStateFlow()

    // ✅ 이제 Repo의 Flow 직접 사용
    val isSignedIn: StateFlow<Boolean> =
        authRepo.isLoggedInFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    fun login(email: String, pw: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            authRepo.login(email, pw) // 토큰 저장 → isLoggedInFlow 자동 true
            _state.value = UiState.Success
        } catch (e: Exception) {
            _state.value = UiState.Error(e.message ?: "알 수 없는 로그인 오류가 발생했습니다.")
        }
    }

    fun refreshToken() = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            authRepo.refresh() // 토큰 갱신 → Flow 반영
            _state.value = UiState.Success
        } catch (e: Exception) {
            _state.value = UiState.Error(e.message ?: "토큰 갱신에 실패했습니다.")
        }
    }
}
