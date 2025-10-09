package com.example.byeoldori.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.Difficulty
import com.example.byeoldori.data.model.dto.EduStatus
import com.example.byeoldori.data.model.dto.EducationDetailResponse
import com.example.byeoldori.data.model.dto.EducationResponse
import com.example.byeoldori.data.model.dto.LikeToggleResponse
import com.example.byeoldori.data.model.dto.SearchBy
import com.example.byeoldori.data.model.dto.SortBy
import com.example.byeoldori.data.repository.EducationRepository
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
class EducationViewModel @Inject constructor(
    private val repo: EducationRepository
): BaseViewModel() {
    private val _postsState = MutableStateFlow<UiState<List<EducationResponse>>>(UiState.Idle)
    val postsState: StateFlow<UiState<List<EducationResponse>>> = _postsState.asStateFlow()

    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostId: StateFlow<String?> = _selectedPostId.asStateFlow()

    private val _createState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val createState: StateFlow<UiState<Long>> = _createState.asStateFlow()

    private val _postDetail = MutableStateFlow<UiState<EducationResponse>>(UiState.Idle)
    val postDetail: StateFlow<UiState<EducationResponse>> = _postDetail.asStateFlow()

    private val _sort = MutableStateFlow(SortBy.LATEST)
    val sort: StateFlow<SortBy> = _sort.asStateFlow()

    private val _likeState = MutableStateFlow<UiState<LikeToggleResponse>>(UiState.Idle)
    val likeState: StateFlow<UiState<LikeToggleResponse>> = _likeState

    private val _likedIds = MutableStateFlow<Set<String>>(emptySet())
    val likedIds: StateFlow<Set<String>> = _likedIds

    private val _likeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val likeCounts: StateFlow<Map<String, Int>> = _likeCounts

    private val _detail = MutableStateFlow<UiState<EducationDetailResponse>>(UiState.Idle)
    val detail: StateFlow<UiState<EducationDetailResponse>> = _detail

    private val _searchBy = MutableStateFlow(SearchBy.TITLE)
    val searchBy: StateFlow<SearchBy> = _searchBy.asStateFlow()

    private val _keyword = MutableStateFlow<String?>(null)
    val keyword: StateFlow<String?> = _keyword.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        _postsState.value = UiState.Loading
        runCatching {
            repo.getAllEducations(
                sortBy = _sort.value,
                searchBy = _searchBy.value,
                keyword = _keyword.value
            )
        }.onSuccess { list ->
            _postsState.value = UiState.Success(list)
        }.onFailure { t ->
            val ex = (t as? Exception) ?: Exception(t.message ?: "error", t)
            _createState.value = UiState.Error(handleException(ex))
        }
    }

    fun setSort(sortBy: SortBy) {
        if (_sort.value == sortBy) return
        _sort.value = sortBy
        loadPosts()
    }

    fun setSearch(searchBy: SearchBy, keyword: String?) {
        _searchBy.value = searchBy
        _keyword.value = keyword?.takeIf { it.isNotBlank() }
        loadPosts()
    }

    fun selectPost(id: String) { _selectedPostId.value = id }
    fun clearSelection() { _selectedPostId.value = null }

    val selectedPost: StateFlow<EducationResponse?> =
        combine(postsState, selectedPostId) { state, id ->
            if (state is UiState.Success && id != null) {
                state.data.firstOrNull { it.id.toString() == id }
            } else null
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadEducationDetail(postId: Long) = viewModelScope.launch {
        _detail.value = UiState.Loading
        runCatching { repo.getEducationDetail(postId) }
            .onSuccess { _detail.value = UiState.Success(it) }
            .onFailure { t ->
                val ex = (t as? Exception) ?: Exception(t.message ?: "알 수 없는 오류", t)
                _detail.value = UiState.Error(handleException(ex))
            }
    }

    fun resetCreateState() {
        _createState.value = UiState.Idle
    }

    fun createEducation(
        title: String,
        content: String,
        summary: String,
        difficulty: Difficulty,
        tags: String,
        status: EduStatus,
        imageUrls: List<String> = emptyList()
    ) = viewModelScope.launch {
        _createState.value = UiState.Loading
        runCatching {
            repo.createEducationPost(
                title = title,
                content = content,
                summary = summary,
                difficulty = difficulty,
                tags = tags,
                status = status,
                imageUrls = imageUrls
            )
        }.onSuccess { newId ->
            Log.d("EducationVM", "교육 글 작성 성공 id=$newId")
            _createState.value = UiState.Success(newId)
            loadPosts() // 목록 갱신
        }.onFailure { e ->
            _createState.value = UiState.Error(e.message ?: "교육 글 작성 오류")
        }
    }


    fun toggleLike(
        postId: Long,
        onResult: ((LikeToggleResponse) -> Unit)? = null
    ) = viewModelScope.launch {
        val key = "edu:$postId"
        _likeState.value = UiState.Loading

        runCatching { repo.toggleLike(postId) }
            .onSuccess { res ->
                _likeState.value = UiState.Success(res)

                //likeCount, liked 상태 반영
                _likeCounts.update { it + (postId.toString() to res.likes.toInt()) }
                _likedIds.update { ids -> if (res.liked) ids + key else ids - key }

                //목록 갱신
                (_postsState.value as? UiState.Success)?.let { cur ->
                    _postsState.value = UiState.Success(
                        cur.data.map { p ->
                            if (p.id == postId)
                                p.copy(likeCount = res.likes.toInt(), liked = res.liked)
                            else p
                        }
                    )
                }

                //상세 갱신
                (_detail.value as? UiState.Success)?.let { cur ->
                    if (cur.data.id == postId) {
                        _detail.value = UiState.Success(
                            cur.data.copy(likeCount = res.likes.toInt(), liked = res.liked)
                        )
                    }
                }
                onResult?.invoke(res)
                Log.d("EducationVM", "좋아요 성공: liked=${res.liked}, likes=${res.likes}")
            }
            .onFailure { e ->
                _likeState.value = UiState.Error("좋아요 토글 실패: ${e.message}")
                Log.e("EducationVM", "좋아요 실패", e)
            }
    }

}