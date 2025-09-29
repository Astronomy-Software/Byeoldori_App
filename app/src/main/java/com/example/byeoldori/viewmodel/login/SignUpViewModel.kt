package com.example.byeoldori.viewmodel.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.SignUpRequest
import com.example.byeoldori.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 회원가입 / 중복확인 상태
sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Loading : SignUpUiState()
    data class Success(val message: String) : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

// 이메일 인증 상태
sealed class VerificationUiState {
    object Idle : VerificationUiState()
    object Loading : VerificationUiState()
    data class Success(val message: String) : VerificationUiState()
    data class Error(val message: String) : VerificationUiState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState

    private val _verificationState = MutableStateFlow<VerificationUiState>(VerificationUiState.Idle)
    val verificationState: StateFlow<VerificationUiState> = _verificationState

    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var passwordConfirm = mutableStateOf("")
    var name = mutableStateOf("")
    var phone = mutableStateOf("")

    var agreePolicy = mutableStateOf(false)
    var agreeProfile = mutableStateOf(false)
    var agreeLocation = mutableStateOf(false)
    var agreeMarketing = mutableStateOf(false)

    private var consents: SignUpRequest.Consents? = null

    fun getEmail(): String = email.value

    fun saveConsents(policy: Boolean, profile: Boolean, location: Boolean, marketing: Boolean) {
        consents = SignUpRequest.Consents(
            termsOfService = policy,
            privacyPolicy = profile,
            location = location,
            marketing = marketing
        )
    }

    fun getConsents(): SignUpRequest.Consents? = consents

    /** 이메일 인증 처리 */
    fun verifyEmail(token: String) = viewModelScope.launch {
        _verificationState.value = VerificationUiState.Loading
        try {
            val resp = repo.verifyEmail(token)
            if (resp.success) {
                _verificationState.value = VerificationUiState.Success(resp.message ?: "이메일 인증 완료")
            } else {
                _verificationState.value = VerificationUiState.Error(resp.message ?: "이메일 인증 실패")
            }
        } catch (e: Exception) {
            _verificationState.value = VerificationUiState.Error(e.message ?: "이메일 인증 중 오류 발생")
        }
    }

    /** 회원가입 */
    fun signUp() = viewModelScope.launch {
        _uiState.value = SignUpUiState.Loading
        try {
            val req = SignUpRequest(
                email = email.value,
                password = password.value,
                passwordConfirm = passwordConfirm.value,
                name = name.value,
                phone = phone.value,
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
