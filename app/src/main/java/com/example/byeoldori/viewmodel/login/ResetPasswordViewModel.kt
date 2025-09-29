package com.example.byeoldori.viewmodel.login

import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.ResetPasswordToEmailRequest
import com.example.byeoldori.data.repository.AuthRepository
import com.example.byeoldori.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ResetPasswordUiState {
    data object Idle : ResetPasswordUiState()
    data object Loading : ResetPasswordUiState()
    data class Success(val message: String) : ResetPasswordUiState()
    data class Error(val message: String) : ResetPasswordUiState()
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val repo: AuthRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val state: StateFlow<ResetPasswordUiState> = _state

    fun resetPassword(email: String, name: String, phone: String) = viewModelScope.launch {
        _state.value = ResetPasswordUiState.Loading
        try {
            val req = ResetPasswordToEmailRequest(email, name, phone)
            val resp = repo.resetPasswordToEmail(req) // 성공(200)일 때만 여기 도달

            _state.value = ResetPasswordUiState.Success(resp.message)
        } catch (e: Exception) {
            _state.value = ResetPasswordUiState.Error(handleException(e))
        }
    }
}