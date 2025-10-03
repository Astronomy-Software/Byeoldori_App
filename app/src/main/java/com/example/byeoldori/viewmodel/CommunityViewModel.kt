package com.example.byeoldori.viewmodel

import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.CommunityType
import com.example.byeoldori.data.model.dto.Post
import com.example.byeoldori.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repo: CommunityRepository
): BaseViewModel() {

    private val _postsState = MutableStateFlow<UiState<List<Post>>>(UiState.Idle)
    val postsState: StateFlow<UiState<List<Post>>> = _postsState.asStateFlow()

    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostId: StateFlow<String?> = _selectedPostId.asStateFlow()

    private val _createState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val createState: StateFlow<UiState<Long>> = _createState.asStateFlow()

    fun loadPosts(type: CommunityType) = viewModelScope.launch {
        _postsState.value = UiState.Loading
        try {
            val posts = repo.getAllPosts(type)
            _postsState.value = UiState.Success(posts)
        } catch (e: Exception) {
            _postsState.value = UiState.Error(handleException(e))
        }
    }

    fun selectPost(id: String) { _selectedPostId.value = id }
    fun clearSelection() { _selectedPostId.value = null }

//    val selectedPost: StateFlow<Post?> =
//        combine(postsState, selectedPostId) { state, id ->
//            if (state is UiState.Success && id != null) {
//                state.data.firstOrNull { it.id.toString() == id }
//            } else null
//        }.stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = null
//        )
}
