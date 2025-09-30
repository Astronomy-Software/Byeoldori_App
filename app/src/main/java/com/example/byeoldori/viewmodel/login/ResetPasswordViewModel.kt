package com.example.byeoldori.viewmodel.login

import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.ResetPasswordToEmailRequest
import com.example.byeoldori.data.repository.AuthRepository
import com.example.byeoldori.viewmodel.BaseViewModel
import com.example.byeoldori.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val repo: AuthRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow<UiState<String>>(UiState.Idle)
    val state: StateFlow<UiState<String>> = _state

    fun resetPassword(email: String, name: String, phone: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val req = ResetPasswordToEmailRequest(email, name, phone)
            val resp = repo.resetPasswordToEmail(req) // 성공(200)일 때만 여기 도달

            _state.value = UiState.Success(resp.message)
        } catch (e: Exception) {
            _state.value = UiState.Error(handleException(e))
        }
    }
}