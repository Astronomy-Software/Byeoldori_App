package com.example.byeoldori.viewmodel.login

import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.FindEmailRequest
import com.example.byeoldori.data.repository.AuthRepository
import com.example.byeoldori.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class FindEmailUiState {
    data object Idle : FindEmailUiState()
    data object Loading : FindEmailUiState()
    data class Success(val emails: List<String>) : FindEmailUiState()
    data class Error(val message: String) : FindEmailUiState()
}

@HiltViewModel
class FindEmailViewModel @Inject constructor(
    private val repo: AuthRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow<FindEmailUiState>(FindEmailUiState.Idle)
    val state: StateFlow<FindEmailUiState> = _state

    fun findEmail(name: String, phone: String) = viewModelScope.launch {
        _state.value = FindEmailUiState.Loading
        try {
            val req = FindEmailRequest(name = name, phone = phone)
            val emails: List<String> = repo.findEmail(req)

            _state.value = FindEmailUiState.Success(emails)
        } catch (e: Exception) {
            _state.value = FindEmailUiState.Error(handleException(e))
        }
    }
}