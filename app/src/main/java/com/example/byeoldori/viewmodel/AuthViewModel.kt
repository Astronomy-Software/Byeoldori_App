package com.example.byeoldori.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val _isSignedIn = MutableStateFlow(true)  // TODO: 실제 토큰/세션으로 초기화
    val isSignedIn: StateFlow<Boolean> = _isSignedIn

    fun signIn() { _isSignedIn.value = true }
    fun signOut() { _isSignedIn.value = false }
}
