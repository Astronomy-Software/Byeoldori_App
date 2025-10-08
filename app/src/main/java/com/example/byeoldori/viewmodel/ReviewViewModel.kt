package com.example.byeoldori.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.LikeToggleResponse
import com.example.byeoldori.data.model.dto.ReviewResponse
import com.example.byeoldori.data.model.dto.SortBy
import com.example.byeoldori.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repo: ReviewRepository
): BaseViewModel() {
    private val _postsState = MutableStateFlow<UiState<List<ReviewResponse>>>(UiState.Idle)
    val postsState: StateFlow<UiState<List<ReviewResponse>>> = _postsState.asStateFlow()

    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostId: StateFlow<String?> = _selectedPostId.asStateFlow()

    private val _createState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val createState: StateFlow<UiState<Long>> = _createState.asStateFlow()

    private val _postDetail = MutableStateFlow<UiState<ReviewResponse>>(UiState.Idle)
    val postDetail: StateFlow<UiState<ReviewResponse>> = _postDetail.asStateFlow()

    private val _sort = MutableStateFlow(SortBy.LATEST)
    val sort: StateFlow<SortBy> = _sort.asStateFlow()

    private val _likeState = MutableStateFlow<UiState<LikeToggleResponse>>(UiState.Idle)
    val likeState: StateFlow<UiState<LikeToggleResponse>> = _likeState

    private val _likedIds = MutableStateFlow<Set<String>>(emptySet())
    val likedIds: StateFlow<Set<String>> = _likedIds

    private val _likeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val likeCounts: StateFlow<Map<String, Int>> = _likeCounts

    init {
        //restoreLikedFromLocal()
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        _postsState.value = UiState.Loading
        try {
            val posts = repo.getAllReviews(sort.value)
            _postsState.value = UiState.Success(posts)
            _likeCounts.update { it + posts.associate { p -> p.id.toString() to p.likeCount } }
        } catch (e: Exception) {
            _postsState.value = UiState.Error(handleException(e))
        }
    }

    fun setSort(sortBy: SortBy) {
        if (_sort.value == sortBy) return
        _sort.value = sortBy
        loadPosts()
    }
    fun selectPost(id: String) { _selectedPostId.value = id }
    fun clearSelection() { _selectedPostId.value = null }

    val selectedPost: StateFlow<ReviewResponse?> =
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
            val post = repo.getReviewDetail(id)
            _postDetail.value = UiState.Success(post)
        } catch (e: Exception) {
            _postDetail.value = UiState.Error(handleException(e))
        }
    }

    fun createPost(
        title: String,
        content: String,
        location: String,
        target: String,
        equipment: String,
        observationDate: String,
        score: Int,
        siteId: Long? = null,
        images: List<Uri> = emptyList()
    ) = viewModelScope.launch {
        _createState.value = UiState.Loading
        runCatching {
            val imageUrls = images.map { it.toString() }
            repo.createReviewPost(
                title = title,
                content = content,
                location = location,
                target = target,
                equipment = equipment,
                observationDate = observationDate,
                score = score,
                observationSiteId = siteId,
                imageUrls = imageUrls
            )
        }.onSuccess { newId ->
            _createState.value = UiState.Success(newId)
            loadPosts()
        }.onFailure { e ->
            _createState.value = UiState.Error(e.message ?: "리뷰 생성 실패")
        }
    }

}