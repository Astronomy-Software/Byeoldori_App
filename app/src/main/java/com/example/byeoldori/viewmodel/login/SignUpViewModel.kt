package com.example.byeoldori.viewmodel.login

import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.SignUpRequest
import com.example.byeoldori.data.repository.AuthRepository
import com.example.byeoldori.viewmodel.BaseViewModel
import com.example.byeoldori.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repo: AuthRepository
) : BaseViewModel() {

    private val _consentEvent = MutableSharedFlow<Unit>()
    val consentEvent: SharedFlow<Unit> = _consentEvent

    private val _signUpEvent = MutableSharedFlow<String>()
    val signUpEvent: SharedFlow<String> = _signUpEvent

    private val _signUpState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val signUpState: StateFlow<UiState<String>> = _signUpState.asStateFlow()

    private val _verificationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val verificationState: StateFlow<UiState<String>> = _verificationState.asStateFlow()

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
        _consentEvent.emit(Unit)
    }

    /** 이메일 인증 처리 */
    fun verifyEmail(token: String) = viewModelScope.launch {
        _verificationState.value = UiState.Loading
        try {
            val resp = repo.verifyEmail(token)
            if (resp.success) {
                _verificationState.value = UiState.Success(resp.message)
            } else {
                _verificationState.value = UiState.Error(resp.message)
            }
        } catch (e: Exception) {
            _verificationState.value = UiState.Error(handleException(e))
        }
    }

    /** 회원가입 */
    fun signUp() = viewModelScope.launch {
        _signUpState.value = UiState.Loading
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
                _signUpState.value = UiState.Success(resp.message)
            } else {
                _signUpState.value = UiState.Error(resp.message)
            }
        } catch (e: Exception) {
            _signUpState.value = UiState.Error(handleException(e))
        }
    }
}

