package com.example.byeoldori.viewmodel.login

import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.SignUpRequest
import com.example.byeoldori.data.repository.AuthRepository
import com.example.byeoldori.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 회원가입 상태
sealed class SignUpUiState {
    data object Idle : SignUpUiState()
    data object Loading : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

// 이메일 인증 상태
sealed class VerificationUiState {
    data object Idle : VerificationUiState()
    data object Loading : VerificationUiState()
    data class Success(val message: String) : VerificationUiState()
    data class Error(val message: String) : VerificationUiState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repo: AuthRepository
) : BaseViewModel() {

    // ✅ 일회성 이벤트
    private val _consentEvent = MutableSharedFlow<Unit>()
    val consentEvent: SharedFlow<Unit> = _consentEvent

    private val _signUpEvent = MutableSharedFlow<String>() // 성공 메시지 이벤트
    val signUpEvent: SharedFlow<String> = _signUpEvent

    // ✅ 상태
    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _verificationState = MutableStateFlow<VerificationUiState>(VerificationUiState.Idle)
    val verificationState: StateFlow<VerificationUiState> = _verificationState.asStateFlow()

    // 입력 값
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val passwordConfirm = MutableStateFlow("")
    val name = MutableStateFlow("")
    val phone = MutableStateFlow("")

    // 동의 정보
    val agreePolicy = MutableStateFlow(false)
    val agreeProfile = MutableStateFlow(false)
    val agreeLocation = MutableStateFlow(false)
    val agreeMarketing = MutableStateFlow(false)

    private var consents: SignUpRequest.Consents = SignUpRequest.Consents(
        termsOfService = false,
        privacyPolicy = false,
        location = false,
        marketing = false
    )

    fun getEmail(): String = email.value

    fun saveConsentsAndProceed() = viewModelScope.launch {
        consents = SignUpRequest.Consents(
            termsOfService = agreePolicy.value,
            privacyPolicy = agreeProfile.value,
            location = agreeLocation.value,
            marketing = agreeMarketing.value
        )
        _consentEvent.emit(Unit) // ✅ 이벤트 발행
    }

    /** 이메일 인증 처리 */
    fun verifyEmail(token: String) = viewModelScope.launch {
        _verificationState.value = VerificationUiState.Loading
        try {
            val resp = repo.verifyEmail(token)
            if (resp.success) {
                _verificationState.value = VerificationUiState.Success(resp.message)
            } else {
                _verificationState.value = VerificationUiState.Error(resp.message)
            }
        } catch (e: Exception) {
            _verificationState.value = VerificationUiState.Error(handleException(e))
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
                consents = consents
            )

            val resp = repo.signUp(req)
            if (resp.success) {
                _signUpEvent.emit(resp.message)
                _uiState.value = SignUpUiState.Idle
            } else {
                _uiState.value = SignUpUiState.Error(resp.message)
            }
        } catch (e: Exception) {
            _uiState.value = SignUpUiState.Error(handleException(e))
        }
    }
}
