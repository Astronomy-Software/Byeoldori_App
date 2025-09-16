package com.example.byeoldori.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // 로그인 여부 (임시: Repo에 isLoggedInFlow가 없으므로 VM에서 관리)
    private val _signedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _signedIn.asStateFlow()

    fun login(email: String, pw: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            // TokenData 반환 (성공 시 예외 없음)
            authRepo.login(email, pw)
            _signedIn.value = true
            _state.value = UiState.Success
        } catch (e: Exception) {
            _state.value = UiState.Error(e.message ?: "알 수 없는 로그인 오류가 발생했습니다.")
        }
    }

    fun signUp(
        email: String,
        password: String,
        passwordConfirm: String,
        name: String,
        phone: String,
        nickname: String,
        birthdate: String
    ) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            authRepo.signUp(
                com.example.byeoldori.data.model.dto.SignUpRequest(
                    email = email,
                    password = password,
                    passwordConfirm = passwordConfirm,
                    name = name,
                    phone = phone,
                    nickname = nickname,
                )
            )
            // 보통 회원가입 후 자동 로그인은 별도 호출
            _state.value = UiState.Success
        } catch (e: Exception) {
            _state.value = UiState.Error(e.message ?: "회원가입 중 오류가 발생했습니다.")
        }
    }

    fun refreshToken() = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            authRepo.refresh()
            _signedIn.value = true
            _state.value = UiState.Success
        } catch (e: Exception) {
            // 리프레시 실패 시 토큰 만료로 간주
            _signedIn.value = false
            _state.value = UiState.Error(e.message ?: "토큰 갱신에 실패했습니다.")
        }
    }

    fun signOut() = viewModelScope.launch {
        authRepo.logout()
        _signedIn.value = false
        _state.value = UiState.Idle
    }
}
