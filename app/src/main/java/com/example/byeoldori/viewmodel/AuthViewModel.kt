package com.example.byeoldori.viewmodel

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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : BaseViewModel() {
    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state.asStateFlow()
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
            _state.value = UiState.Success(Unit)
        } catch (e: Exception) {
            _state.value = UiState.Error(handleException(e))
        }
    }

    fun refreshToken() = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            authRepo.refresh() // 토큰 갱신 → Flow 반영
            _state.value = UiState.Success(Unit)
        } catch (e: Exception) {
            _state.value = UiState.Error(e.message ?: "토큰 갱신에 실패했습니다.")
        }
    }
}
