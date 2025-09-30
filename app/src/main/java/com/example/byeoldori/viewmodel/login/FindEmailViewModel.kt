package com.example.byeoldori.viewmodel.login

import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.FindEmailRequest
import com.example.byeoldori.data.repository.AuthRepository
import com.example.byeoldori.viewmodel.BaseViewModel
import com.example.byeoldori.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindEmailViewModel @Inject constructor(
    private val repo: AuthRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow<UiState<List<String>>>(UiState.Idle)
    val state: StateFlow<UiState<List<String>>> = _state

    fun findEmail(name: String, phone: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val req = FindEmailRequest(name = name, phone = phone)
            val emails: List<String> = repo.findEmail(req)

            _state.value = UiState.Success(emails)
        } catch (e: Exception) {
            _state.value = UiState.Error(handleException(e))
        }
    }
}