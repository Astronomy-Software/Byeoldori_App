package com.example.byeoldori.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI 상태를 안전하게 표현하기 위한 sealed class를 정의합니다.
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

    // _state는 ViewModel 내부에서만 값을 변경하도록 private으로 선언합니다.
    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    // state는 외부에서 관찰만 가능하도록 StateFlow로 노출합니다.
    val state: StateFlow<UiState> = _state

    // AuthRepository의 isLoggedInFlow를 ViewModel의 상태로 노출합니다.
    val isSignedIn: Boolean = true
    // 임시로 바꿔뒀음


    fun login(email: String, pw: String) = viewModelScope.launch {
        _state.value = UiState.Loading

        // 로그인 성공 또는 실패 결과에 따라 UI 상태를 업데이트합니다.
        // authRepo의 login 함수가 Result<Unit>을 반환한다고 가정합니다.
        authRepo.login(email, pw)
            .fold(
                onSuccess = { _state.value = UiState.Success },
                onFailure = {
                    _state.value = UiState.Error(it.message ?: "알 수 없는 로그인 오류가 발생했습니다.")
                }
            )
    }

    fun signOut() = viewModelScope.launch {
        authRepo.logout()
    }
}