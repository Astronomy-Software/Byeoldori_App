package com.example.byeoldori.viewmodel

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.CommunityType
import com.example.byeoldori.data.model.dto.FreePostResponse
import com.example.byeoldori.data.model.dto.LikeToggleResponse
import com.example.byeoldori.data.model.dto.SortBy
import com.example.byeoldori.data.repository.CommunityRepository
import com.example.byeoldori.ui.components.community.likedKeyFree
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

    private val _postsState = MutableStateFlow<UiState<List<FreePostResponse>>>(UiState.Idle)
    val postsState: StateFlow<UiState<List<FreePostResponse>>> = _postsState.asStateFlow()

    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostId: StateFlow<String?> = _selectedPostId.asStateFlow()

    private val _createState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val createState: StateFlow<UiState<Long>> = _createState.asStateFlow()

    private val _postDetail = MutableStateFlow<UiState<FreePostResponse>>(UiState.Idle)
    val postDetail: StateFlow<UiState<FreePostResponse>> = _postDetail.asStateFlow()

    private val _sort = MutableStateFlow(SortBy.LATEST)
    val sort: StateFlow<SortBy> = _sort.asStateFlow()

    private val _likeState = MutableStateFlow<UiState<LikeToggleResponse>>(UiState.Idle)
    val likeState: StateFlow<UiState<LikeToggleResponse>> = _likeState

    private val _likedIds = MutableStateFlow<Set<String>>(emptySet())
    val likedIds: StateFlow<Set<String>> = _likedIds

    init { loadPosts() }

    fun loadPosts() = viewModelScope.launch {
        _postsState.value = UiState.Loading
        try {
            val posts = repo.getAllPosts(sort.value)
            _postsState.value = UiState.Success(posts)
        } catch (e: Exception) {
            _postsState.value = UiState.Error(handleException(e))
        }
    }

    fun setSort(sortBy: SortBy) {
        if (_sort.value == sortBy) return
        _sort.value = sortBy
        loadPosts()                           // 정렬 변경 시 재호출
    }

    fun selectPost(id: String) { _selectedPostId.value = id }
    fun clearSelection() { _selectedPostId.value = null }

    val selectedPost: StateFlow<FreePostResponse?> =
        combine(postsState, selectedPostId) { state, id ->
            if (state is UiState.Success && id != null) {
                state.data.firstOrNull { it.id.toString() == id }
            } else null
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadPostDetail(id: Long) = viewModelScope.launch {
        _postDetail.value = UiState.Loading
        try {
            val post = repo.getPostDetail(id)
            _postDetail.value = UiState.Success(post)
        } catch (e: Exception) {
            _postDetail.value = UiState.Error(handleException(e))
        }
    }

    fun createPost(title: String, content: String,images: List<Uri> = emptyList()) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            runCatching {
                val imageUrls = images.map { it.toString() }
                repo.createFreePost(title, content, imageUrls)
            }.onSuccess { newId ->
                _createState.value = UiState.Success(newId)
                loadPosts()
            }.onFailure { e ->
                _createState.value = UiState.Error(e.message ?: "게시글 생성 실패")
            }
        }
    }
    fun clearCreateState() { _createState.value = UiState.Idle }

    fun toggleLike(postId: Long) = viewModelScope.launch {
        _likeState.value = UiState.Loading
        runCatching {
            repo.toggleLike(postId)
        }.onSuccess { res ->
            _likeState.value = UiState.Success(res)
            Log.d("CommunityVM", "좋아요 성공: liked=${res.liked}, likes=${res.likes}")

            val key = likedKeyFree(postId.toString())

            val current = _postsState.value
            if (current is UiState.Success) {
                val updated = current.data.map { post ->
                    if (post.id == postId) post.copy(likeCount = res.likes.toInt())
                    else post
                }
                _postsState.value = UiState.Success(updated)
            }
        }.onFailure { e ->
            if (e is retrofit2.HttpException) {
                val code = e.code()
                val body = e.response()?.errorBody()?.string()
                Log.e("CommunityVM", "좋아요 실패: code=$code, body=$body")
            } else {
                Log.e("CommunityVM", "좋아요 실패: ${e.message}", e)
            }
            _likeState.value = UiState.Error("좋아요 토글 실패: ${e.message}")
        }
    }

    fun toggleLikedLocal(key: String) {
        val cur = _likedIds.value
        _likedIds.value = if (key in cur) cur - key else cur + key
    }

}