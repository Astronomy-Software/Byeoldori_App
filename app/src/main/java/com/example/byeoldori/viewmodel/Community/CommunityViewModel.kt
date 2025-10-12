package com.example.byeoldori.viewmodel.Community

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.data.repository.FreeRepository
import com.example.byeoldori.ui.components.community.likedKeyFree
import com.example.byeoldori.viewmodel.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repo: FreeRepository
): BaseViewModel() {

    private val _postsState = MutableStateFlow<UiState<List<FreePostResponse>>>(UiState.Idle)
    val postsState: StateFlow<UiState<List<FreePostResponse>>> = _postsState.asStateFlow()

    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostId: StateFlow<String?> = _selectedPostId.asStateFlow()

    private val _createState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val createState: StateFlow<UiState<Long>> = _createState.asStateFlow()

    private val _postDetail = MutableStateFlow<UiState<PostDetailResponse>>(UiState.Idle)
    val postDetail = _postDetail.asStateFlow()

    private val _sort = MutableStateFlow(SortBy.LATEST)
    val sort: StateFlow<SortBy> = _sort.asStateFlow()

    private val _likeState = MutableStateFlow<UiState<LikeToggleResponse>>(UiState.Idle)
    val likeState: StateFlow<UiState<LikeToggleResponse>> = _likeState

    private val _likedIds = MutableStateFlow<Set<String>>(emptySet())
    val likedIds: StateFlow<Set<String>> = _likedIds

    private val _likeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val likeCounts: StateFlow<Map<String, Int>> = _likeCounts

    private val _commentCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val commentCounts: StateFlow<Map<String, Int>> = _commentCounts

    init {
        restoreLikedFromLocal()
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        _postsState.value = UiState.Loading
        try {
            val posts = repo.getAllPosts(sort.value)
            _postsState.value = UiState.Success(posts)
            //리스트 받아올 때 현재 개수 반영
            _likeCounts.update { it + posts.associate { p-> p.id.toString() to p.likeCount } }
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
            val post: PostDetailResponse = repo.getPostDetail(id)
            _postDetail.value = UiState.Success(post)
            Log.d("CommunityVM", "게시글 상세 불러오기 성공 id=$id, title=${post.title}")
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
    fun resetPostDetail() { _postDetail.value = UiState.Idle }

    fun toggleLike(
        postId: Long,
        onResult: ((LikeToggleResponse) -> Unit)? = null
    ) = viewModelScope.launch {
        val key = likedKeyFree(postId.toString())
        _likeState.value = UiState.Loading

        runCatching { repo.toggleLike(postId) }
            .onSuccess { res ->
                _likeState.value = UiState.Success(res)
                _likeCounts.update { it + (postId.toString() to res.likes.toInt()) }
                _likedIds.update { ids -> if (res.liked) ids + key else ids - key }
                saveLikedToLocal()

                //목록/상세 동기화
                (_postsState.value as? UiState.Success)?.let { cur ->
                    _postsState.value = UiState.Success(
                        cur.data.map { p ->
                            if (p.id == postId) p.copy(
                                liked = res.liked,
                                likeCount = res.likes.toInt()
                            ) else p
                        }
                    )
                }
                (_postDetail.value as? UiState.Success)?.let { cur ->
                    if (cur.data.id == postId) {
                        _postDetail.value = UiState.Success(
                            cur.data.copy(
                                liked = res.liked,
                                likeCount = res.likes.toInt()
                            )
                        )
                    }
                }

                ///섹션에서 로컬 상태 즉시 반영
                onResult?.invoke(res)
                Log.d("CommunityVM", "좋아요 성공: liked=${res.liked}, likes=${res.likes}")
            }
            .onFailure { e ->
                _likeState.value = UiState.Error("좋아요 토글 실패: ${e.message}")
                Log.e("CommunityVM", "좋아요 실패", e)
            }
    }

    fun saveLikedToLocal() {
        viewModelScope.launch {
            try {
                repo.saveLikedKeys(_likedIds.value)
            } catch (e: Exception) {
                Log.e("CommunityVM", "saveLikedToLocal 실패", e)
            }
        }
    }

    fun restoreLikedFromLocal() {
        viewModelScope.launch {
            try {
                val saved = repo.loadLikedKeys()
                _likedIds.value = saved
                Log.d("CommunityVM", "restoreLikedFromLocal 복원됨: $saved")
            } catch (e: Exception) {
                Log.e("CommunityVM", "restoreLikedFromLocal 실패", e)
            }
        }
    }

    fun findNicknameByAuthorId(authorId: Long): String? {
        val currentPosts = (_postsState.value as? UiState.Success)?.data ?: return null
        return currentPosts.firstOrNull { it.authorId == authorId }?.authorNickname
    }
}