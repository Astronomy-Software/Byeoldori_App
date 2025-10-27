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

    private val MAX_IMAGE_BYTES = 10L * 1024 * 1024 // 10MB

    fun uploadImage(
        file: File,
        token: String? = null,
        onProgress: ((sent: Long,total: Long) -> Unit)? = null
    ) {
        val size = file.length()
        if (size > MAX_IMAGE_BYTES) {
            _uploadState.value = UiState.Error("파일 크기는 10MB를 초과할 수 없습니다.")
            return
        }

        viewModelScope.launch {
            _uploadState.value = UiState.Loading
            try {
                val url = repo.uploadImage(file, token, onProgress)
                if (url != null) {
                    _uploadState.value = UiState.Success(url)
                } else {
                    _uploadState.value = UiState.Error("이미지 업로드 실패")
                }
            } catch (e: IllegalArgumentException) {
                // 레포지토리에서도 동일한 검증을 수행하므로 메시지 통일
                _uploadState.value = UiState.Error(e.message ?: "파일 크기는 10MB를 초과할 수 없습니다.")
            } catch (e: Exception) {
                _uploadState.value = UiState.Error(
                    "이미지 업로드 중 문제가 발생했습니다: ${e.message ?: "알 수 없는 오류"}"
                )
            }
        }
    }

    fun reset() { _uploadState.value = UiState.Idle }
}