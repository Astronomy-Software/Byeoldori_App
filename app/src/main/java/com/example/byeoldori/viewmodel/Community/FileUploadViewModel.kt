package com.example.byeoldori.viewmodel.Community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.repository.FileRepository
import com.example.byeoldori.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileUploadViewModel @Inject constructor(
    private val repo: FileRepository
): ViewModel() {

    private val _uploadState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uploadState: StateFlow<UiState<String>> = _uploadState

    fun uploadImage(file: File, token: String? = null) {
        viewModelScope.launch {
            _uploadState.value = UiState.Loading
            val url = repo.uploadImage(file,token)
            if (url != null) {
                _uploadState.value = UiState.Success(url)
            } else {
                _uploadState.value = UiState.Error("이미지 업로드 실패")
            }
        }
    }

    fun reset() { _uploadState.value = UiState.Idle }
}