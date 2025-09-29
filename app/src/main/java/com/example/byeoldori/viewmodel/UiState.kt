package com.example.byeoldori.viewmodel

// UI에 사용할 전역 UiState 선언
sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<@UnsafeVariance T>()
    data class Error(val message: String) : UiState<Nothing>()
}
