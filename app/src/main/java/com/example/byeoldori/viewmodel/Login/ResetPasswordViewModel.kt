// viewmodel/Login/ResetPasswordViewModel.kt
package com.example.byeoldori.viewmodel.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.ResetPasswordToEmailRequest
import com.example.byeoldori.data.repository.FindAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ResetPasswordUiState {
    object Idle : ResetPasswordUiState()
    object Loading : ResetPasswordUiState()
    data class Success(val message: String) : ResetPasswordUiState()
    data class Error(val message: String) : ResetPasswordUiState()
}


@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val repo: FindAccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val state: StateFlow<ResetPasswordUiState> = _state

    fun resetPassword(email: String) = viewModelScope.launch {
        _state.value = ResetPasswordUiState.Loading
        try {
            val req = ResetPasswordToEmailRequest(email)
            val resp = repo.resetPasswordToEmail(req)

            if (resp.success) {
                _state.value = ResetPasswordUiState.Success(resp.message ?: "비밀번호 재설정 메일을 보냈습니다.")
            } else {
                _state.value = ResetPasswordUiState.Error(resp.message ?: "비밀번호 재설정 실패")
            }
        } catch (e: Exception) {
            _state.value = ResetPasswordUiState.Error("네트워크 오류: ${e.message}")
        }
    }
}
