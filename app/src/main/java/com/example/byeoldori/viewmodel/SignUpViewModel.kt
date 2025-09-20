package com.example.byeoldori.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.SignUpRequest
import com.example.byeoldori.data.repository.SignUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI 상태 표현
sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Loading : SignUpUiState()
    data class Success(val message: String) : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repo: SignUpRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState

    // 동의 정보 저장용
    private var consents: SignUpRequest.Consents? = null

    fun saveConsents(policy: Boolean, profile: Boolean, location: Boolean, marketing: Boolean) {
        consents = SignUpRequest.Consents(
            termsOfService = policy,
            privacyPolicy = profile,
            location = location,
            marketing = marketing
        )
    }

    fun getConsents(): SignUpRequest.Consents? = consents

    /** 이메일 중복 확인 */
    fun checkEmail(email: String) = viewModelScope.launch {
        _uiState.value = SignUpUiState.Loading
        try {
            val resp = repo.checkEmail(email)
            if (resp.success) {
                val exists = resp.data?.exists ?: false
                if (exists) _uiState.value = SignUpUiState.Error("이미 사용 중인 이메일입니다.")
                else _uiState.value = SignUpUiState.Success("사용 가능한 이메일입니다.")
            } else {
                _uiState.value = SignUpUiState.Error(resp.message ?: "이메일 중복 확인 실패")
            }
        } catch (e: Exception) {
            _uiState.value = SignUpUiState.Error(e.message ?: "이메일 중복 확인 중 오류 발생")
        }
    }

    /** 닉네임 중복 확인 */
    fun checkNickname(nickname: String) = viewModelScope.launch {
        _uiState.value = SignUpUiState.Loading
        try {
            val resp = repo.checkNickname(nickname)
            if (resp.success) {
                val exists = resp.data?.exists ?: false
                if (exists) _uiState.value = SignUpUiState.Error("이미 사용 중인 닉네임입니다.")
                else _uiState.value = SignUpUiState.Success("사용 가능한 닉네임입니다.")
            } else {
                _uiState.value = SignUpUiState.Error(resp.message ?: "닉네임 중복 확인 실패")
            }
        } catch (e: Exception) {
            _uiState.value = SignUpUiState.Error(e.message ?: "닉네임 중복 확인 중 오류 발생")
        }
    }

    /** 이메일 인증 처리 */
    fun verifyEmail(token: String) = viewModelScope.launch {
        _uiState.value = SignUpUiState.Loading
        try {
            val resp = repo.verifyEmail(token)
            if (resp.success) {
                _uiState.value = SignUpUiState.Success(resp.message ?: "이메일 인증 완료")
            } else {
                _uiState.value = SignUpUiState.Error(resp.message ?: "이메일 인증 실패")
            }
        } catch (e: Exception) {
            _uiState.value = SignUpUiState.Error(e.message ?: "이메일 인증 중 오류 발생")
        }
    }

    /** 회원가입 - 원시 데이터 받아서 Request 조립 */
    fun signUp(
        email: String,
        password: String,
        passwordConfirm: String,
        name: String,
        phone: String,
    ) = viewModelScope.launch {
        _uiState.value = SignUpUiState.Loading
        try {
            val req = SignUpRequest(
                email = email,
                password = password,
                passwordConfirm = passwordConfirm,
                name = name,
                phone = phone,
                consents = consents ?: error("동의 정보 없음")
            )

            val resp = repo.signUp(req)
            if (resp.success) {
                _uiState.value = SignUpUiState.Success(resp.message ?: "회원가입 성공")
            } else {
                _uiState.value = SignUpUiState.Error(resp.message ?: "회원가입 실패")
            }
        } catch (e: Exception) {
            _uiState.value = SignUpUiState.Error(e.message ?: "회원가입 중 오류 발생")
        }
    }
}
