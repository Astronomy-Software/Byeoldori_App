package com.example.byeoldori.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.CommentsPageResponse
import com.example.byeoldori.data.repository.CommentsRepository
import com.example.byeoldori.domain.Community.ReviewComment
import com.example.byeoldori.ui.mapper.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val repo: CommentsRepository
) : ViewModel() {

    private var postIdLong: Long? = null
    private var postIdStr: String = ""

    private var page = 1
    private var lastPage = Int.MAX_VALUE

    private val _comments = MutableStateFlow<UiState<List<ReviewComment>>>(UiState.Idle)
    val comments: StateFlow<UiState<List<ReviewComment>>> = _comments

    private val _commentCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val commentCounts: StateFlow<Map<String, Int>> = _commentCounts

    private val buffer = mutableListOf<ReviewComment>()

    /** 상세 진입 시 호출 */
    fun start(postId: String) {
        postIdStr = postId
        postIdLong = postId.toLongOrNull()
        page = 1
        lastPage = Int.MAX_VALUE
        buffer.clear()
        load(refresh = true)
    }

    fun load(refresh: Boolean = false) {
        val pid = postIdLong ?: return
        if (refresh) {
            _comments.value = UiState.Loading
            page = 1
            lastPage = Int.MAX_VALUE
            buffer.clear()
        }
        if (page > lastPage) return

        viewModelScope.launch {
            val res = repo.getComments(pid, page = page, size = 15)
            res.onSuccess { pageRes ->
                applyPage(pageRes)
                _commentCounts.value = _commentCounts.value + (postIdStr to pageRes.totalElements)
            }.onFailure { e ->
                _comments.value = UiState.Error(e.message ?: "댓글 목록 로드 실패")
            }
        }
    }

    private fun applyPage(pageRes: CommentsPageResponse) {
        val newItems = pageRes.content.map { it.toUi(postIdStr) }
        buffer += newItems
        page = pageRes.page + 1
        lastPage = pageRes.totalPages.coerceAtLeast(1)
        _comments.value = UiState.Success(buffer.toList())
    }

    fun submit(content: String, parentId: String? = null, onComplete: (() -> Unit)? = null) {
        val pid = postIdLong ?: return
        viewModelScope.launch {
            val res = repo.createComment(pid, content, parentId = parentId?.toLongOrNull())
            res.onSuccess { created ->
                // 가장 최근에 추가
                buffer.add(0, created.toUi(postIdStr))
                _comments.value = UiState.Success(buffer.toList())

                val cur = _commentCounts.value[postIdStr] ?: 0
                _commentCounts.value = _commentCounts.value + (postIdStr to (cur + 1))
                onComplete?.invoke()
            }.onFailure { e ->
                _comments.value = UiState.Error(e.message ?: "댓글 작성 실패")
            }
        }
    }
}