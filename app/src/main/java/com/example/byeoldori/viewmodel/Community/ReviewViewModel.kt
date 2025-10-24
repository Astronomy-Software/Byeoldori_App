package com.example.byeoldori.viewmodel.Community

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.data.repository.ReviewRepository
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.components.community.review.toDomain
import com.example.byeoldori.viewmodel.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repo: ReviewRepository, @ApplicationContext context: Context
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

    private val _detail = MutableStateFlow<UiState<ReviewDetailResponse>>(UiState.Idle)
    val detail: StateFlow<UiState<ReviewDetailResponse>> = _detail

    private val _scores = MutableStateFlow<Map<String, Int>>(emptyMap())
    val scores: StateFlow<Map<String, Int>> = _scores

    private val _siteStats = MutableStateFlow<ReviewRepository.SiteInfo?>(null)
    val siteStats: StateFlow<ReviewRepository.SiteInfo?> = _siteStats

    private val _authorCache = MutableStateFlow<Map<Long, String>>(emptyMap())
    val authorCache: StateFlow<Map<Long, String>> = _authorCache

    private val prefs = context.getSharedPreferences("thumbnails", Context.MODE_PRIVATE)
    private val _thumbnails = MutableStateFlow<Map<String, String>>(emptyMap())
    val thumbnails: StateFlow<Map<String, String>> = _thumbnails

    fun loadLocalThumbnails() {
        _thumbnails.value = prefs.all.mapValues { it.value.toString() }
    }

    fun registerLocalThumbnail(id: String, url: String?) {
        if (url.isNullOrBlank()) return
        _thumbnails.value = _thumbnails.value + (id to url)
        prefs.edit().putString(id, url).apply()
    }

    fun onListLoaded(list: List<ReviewResponse>) {
        if (list.isEmpty()) return
        val add = list.associate { it.id to (it.authorNickname ?: "익명") }
        _authorCache.value = _authorCache.value + add
    }

    val detailUi: StateFlow<UiState<Review>> =
        combine(detail, authorCache) { d, cache ->
            when (d) {
                is UiState.Idle -> UiState.Idle
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> UiState.Error(d.message ?: "상세 조회 실패")
                is UiState.Success -> {
                    val res = d.data
                    val nick = cache[res.id] ?: "익명"
                    UiState.Success(res.toDomain(author = nick))  //여기서 닉네임 주입
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Idle
        )

    init { loadPosts() }

    fun loadPosts() = viewModelScope.launch {
        _postsState.value = UiState.Loading
        try {
            val posts = repo.getAllReviews(sort.value)
            _postsState.value = UiState.Success(posts)
            _likeCounts.update { it + posts.associate { p -> p.id.toString() to p.likeCount } }
            onListLoaded(posts) //닉네임 관련 캐시
        } catch (e: Exception) {
            _postsState.value = UiState.Error(handleException(e))
        }
    }

    //관측지 탭에서 해당 리뷰를 찾기 위함
    fun reloadReviewsLatest(size: Int = 50) {
        viewModelScope.launch {
            _postsState.value = UiState.Loading
            try {
                val list = repo.getAllReviews(
                    sortBy = SortBy.LATEST,
                    searchBy = SearchBy.TITLE,
                    keyword = null
                )
                _postsState.value = UiState.Success(list)
            } catch (e: Exception) {
                _postsState.value = UiState.Error(e.message ?: "리뷰를 불러오지 못했습니다.")
            }
        }
    }

    fun setSort(sortBy: SortBy) {
        if (_sort.value == sortBy) return
        _sort.value = sortBy
        loadPosts()
    }
    fun selectPost(id: String) { _selectedPostId.value = id }

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

    fun loadReviewDetail(postId: Long) = viewModelScope.launch {
        _detail.value = UiState.Loading
        try {
            val res = repo.getReviewDetail(postId)
            _detail.value = UiState.Success(res)
            //상세의 첫 이미지를 썸네일 캐시에 저장
            registerLocalThumbnail(postId.toString(), res.images.firstOrNull())
        } catch (e: Exception) {
            _detail.value = UiState.Error(handleException(e))
        }
    }

    fun ensureScoreLoaded(id: Long) {
        val key = id.toString()
        if (_scores.value.containsKey(key)) return  // 이미 있으면 스킵
        viewModelScope.launch {
            runCatching { repo.getReviewDetail(id.toLong()) }
                .onSuccess { detail ->
                    val score = detail.review?.score ?: 0
                    _scores.update { it + (key to score) }
                }
                .onFailure { /* 로그 정도 */ }
        }
    }

    fun resetCreateState() {
        _createState.value = UiState.Idle
    }

    fun resetDetail() {
        _detail.value = UiState.Idle
    }

    fun createReview(
        title: String,
        content: String,
        location: String,
        target: String,
        equipment: String,
        observationDate: String,
        score: Int,
        observationSiteId: Long? = null,
        imageUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            runCatching {
                repo.createReviewPost(
                    title = title,
                    content = content,
                    location = location,
                    target = target,
                    equipment = equipment,
                    observationDate = observationDate,
                    score = score,
                    observationSiteId = observationSiteId,
                    imageUrls = imageUrls
                )
            }.onSuccess { newId ->
                Log.d("ReviewVM", "작성 성공 id=$newId")
                _createState.value = UiState.Success(newId)
                registerLocalThumbnail(newId.toString(), imageUrls.firstOrNull()) //작성 직후 첫 이미지르 썸네일로 등록
                ensureScoreLoaded(newId)
                loadPosts()
            }.onFailure { e ->
                _createState.value = UiState.Error(e.message ?: "리뷰 작성 오류")
            }
        }
    }

    fun toggleLike(postId: Long, onResult: (LikeToggleResponse) -> Unit) {
        viewModelScope.launch {
            runCatching { repo.toggleLike(postId) }
                .onSuccess { res ->
                    onResult(res)

                    // 목록 갱신: p.id가 Int면 toLong()해서 비교
                    val cur = _postsState.value
                    if (cur is UiState.Success) {
                        val updated = cur.data.map { p ->
                            if (p.id == postId) { // p.id가 Long, postId도 Long → 비교 OK
                                p.copy(liked = res.liked, likeCount = res.likes.toInt())
                            } else p
                        }
                        _postsState.value = UiState.Success(updated)
                    }

                    // 상세 갱신 (repo 헬퍼 사용)
                    val d = _detail.value
                    if (d is UiState.Success) {
                        _detail.value = UiState.Success(
                            repo.applyLikeToDetail(d.data, res.liked, res.likes.toInt())
                        )
                    }
                }
                .onFailure { t ->
                    _likeState.value = UiState.Error(t.message ?: "알 수 없는 오류")
                }
        }
    }

    suspend fun getSiteInfo(siteId: Long) = repo.getSiteInfo(siteId)

    suspend fun loadSiteInfo(siteId: Long) { _siteStats.value = getSiteInfo(siteId) }

    suspend fun clearSiteInfo() { _siteStats.value = null } //관측지 변경 시 상태 초기화

    fun applyLikeDelta(likedNow: Boolean) {
        val delta = if (likedNow) 1 else -1
        _siteStats.update { curr ->
            curr?.copy(likeCount = (curr.likeCount + delta).coerceAtLeast(0))
        }
    }

    fun updateReview(
        postId: Long,
        title: String,
        content: String,
        location: String,
        target: String,
        equipment: String,
        observationDate: String,
        score: Int,
        observationSiteId: Long? = null,
        imageUrls: List<String> = emptyList(),
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repo.updateReview(
                    postId = postId,
                    title = title,
                    content = content,
                    location = location,
                    target = target,
                    equipment = equipment,
                    observationDate = observationDate,
                    score = score,
                    observationSiteId = observationSiteId,
                    imageUrls = imageUrls
                )
                loadReviewDetail(postId)
                loadPosts()
                onDone()
            } catch (e: Exception) {
                Log.e("ReviewVM", "리뷰 수정 실패: ${e.message}", e)
            }
        }
    }
}