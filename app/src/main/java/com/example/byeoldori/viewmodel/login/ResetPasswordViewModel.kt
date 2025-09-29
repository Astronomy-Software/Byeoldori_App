package com.example.byeoldori.viewmodel.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.ResetPasswordToEmailRequest
import com.example.byeoldori.data.repository.AuthRepository
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
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val state: StateFlow<ResetPasswordUiState> = _state

//    fun resetPassword(email: String, phone: String, name : String) = viewModelScope.launch {
//        _state.value = ResetPasswordUiState.Loading
//        try {
//            val req = ResetPasswordToEmailRequest(email, phone, name)
//            val resp = repo.resetPasswordToEmail(req)
//            Log.d("ResetPasswordVM", "resp.code=${resp.code}, resp.message=${resp.message}")
//
//            if (resp.success) {
//                _state.value = ResetPasswordUiState.Success(resp.message ?: "비밀번호 재설정 메일을 보냈습니다.")
//            } else {
//                if (resp.code == 400) {
//                    _state.value = ResetPasswordUiState.Error("입력한 정보가 올바르지 않습니다.")
//                } else if (!resp.success) {
//                    _state.value = ResetPasswordUiState.Error(resp.message ?: "알 수 없는 오류가 발생했습니다.")
//                } else {
//                    _state.value = ResetPasswordUiState.Success("비밀번호 재설정 메일을 전송했습니다.")
//                }
//            }
//        } catch (e: Exception) {
//            _state.value = ResetPasswordUiState.Error("네트워크 오류: ${e.message}")
//            Log.d("ResetPasswordVM", "resp.code=${e.code}, resp.message=${resp.message}")
//
//        }
//    }
    fun resetPassword(email: String, phone: String, name: String) = viewModelScope.launch {
        _state.value = ResetPasswordUiState.Loading
        try {
            val req = ResetPasswordToEmailRequest(email, phone, name)
            val resp = repo.resetPasswordToEmail(req)

            Log.d("ResetPasswordVM", "resp.code=${resp.code}, resp.message=${resp.message}")

            if (resp.success && resp.code == 200) {
                _state.value = ResetPasswordUiState.Success(resp.message ?: "메일을 보냈습니다.")
            } else if (resp.code == 400) {
                _state.value = ResetPasswordUiState.Error("입력한 정보가 올바르지 않습니다.")
            } else {
                _state.value = ResetPasswordUiState.Error(resp.message ?: "알 수 없는 오류")
            }

        } catch (e: Exception) {
            // 네트워크 오류, JSON 파싱 오류 등
            when (e) {
                is retrofit2.HttpException -> {
                    Log.e("ResetPasswordVM", "HttpException code=${e.code()}, message=${e.message()}")
                    _state.value = ResetPasswordUiState.Error("서버 오류(${e.code()})가 발생했습니다.")
                }
                is java.net.UnknownHostException -> {
                    Log.e("ResetPasswordVM", "네트워크 연결 안 됨: ${e.message}")
                    _state.value = ResetPasswordUiState.Error("네트워크 연결을 확인하세요.")
                }
                else -> {
                    Log.e("ResetPasswordVM", "Unexpected error: ${e.message}")
                    _state.value = ResetPasswordUiState.Error("알 수 없는 오류: ${e.message}")
                }
            }
        }
    }
}
