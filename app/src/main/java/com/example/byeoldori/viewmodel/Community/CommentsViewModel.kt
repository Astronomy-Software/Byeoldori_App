package com.example.byeoldori.viewmodel.Community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.CommentsPageResponse
import com.example.byeoldori.data.model.dto.LikeToggleResponse
import com.example.byeoldori.data.model.dto.PostDetailResponse
import com.example.byeoldori.data.repository.CommentsRepository
import com.example.byeoldori.domain.Community.ReviewComment
import com.example.byeoldori.ui.mapper.toUi
import com.example.byeoldori.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        buffer.clear() //이전 게시글의 댓글 초기화
        load(refresh = true) //댓글 첫 페이지를 새로 불러옴
    }

    /** 현재 게시글의 댓글 페이지를 불러옴 **/
    fun load(refresh: Boolean = false) {
        val pid = postIdLong ?: return
        if (refresh) {
            _comments.value = UiState.Loading
            page = 1
            lastPage = Int.MAX_VALUE
            buffer.clear()
        }
        if (page > lastPage) return //끝까지 다 불러왔으니까 중단

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

    /** 댓글을 버퍼에 누적하고 즉시 업데이트*/
    private fun applyPage(pageRes: CommentsPageResponse) {
        val newItems = pageRes.content.map { it.toUi(postIdStr) }
        buffer += newItems
        page = pageRes.page + 1 //다음 로드때 가져올 페이지 번호
        lastPage = pageRes.totalPages.coerceAtLeast(1)
        _comments.value = UiState.Success(buffer.toList()) //업데이트
    }

    fun submit(content: String, parentId: String? = null, onComplete: (() -> Unit)? = null) {
        val pid = postIdLong ?: return
        viewModelScope.launch {
            val res = repo.createComment(pid, content, parentId = parentId?.toLongOrNull())
            res.onSuccess { created ->
                // 가장 마지막에 추가
                buffer.add(created.toUi(postIdStr))
                _comments.value = UiState.Success(buffer.toList())

                //기존 댓글 수 가져와서 +1
                val cur = _commentCounts.value[postIdStr] ?: 0
                _commentCounts.value = _commentCounts.value + (postIdStr to (cur + 1))
                onComplete?.invoke()
            }.onFailure { e ->
                _comments.value = UiState.Error(e.message ?: "댓글 작성 실패")
            }
        }
    }

    fun toggleLike(
        commentId: Long,
        onResult: (LikeToggleResponse) -> Unit = {}
    ) = viewModelScope.launch {
        val postId = postIdLong ?: return@launch
        try {
            val res = repo.toggleCommentLike(postId, commentId)

            val next = when (val s = _comments.value) {
                is UiState.Success -> {
                    val updated = s.data.map { c ->
                        if (c.id.toLongOrNull() == commentId) {
                            c.copy(
                                liked = res.liked,
                                likeCount = res.likes.toInt()
                            )
                        } else c
                    }
                    UiState.Success(updated)
                }
                else -> s
            }
            _comments.value = next
            onResult(res)
        } catch (_: Exception) { /* 에러 처리 필요시 추가 */ }
    }

    fun delete(
        commentId: Long,
        onComplete: (() -> Unit)? = null
    ) = viewModelScope.launch {
        val postId = postIdLong ?: return@launch
        val response = repo.deleteComment(postId, commentId)

        response.onSuccess {
            val next = when (val s = _comments.value) {
                //s.data는 List<CommentResponse>
                is UiState.Success -> {
                    UiState.Success(
                       s.data.map { comment ->
                           if (comment.id.toLongOrNull() == commentId) {
                               comment.copy(
                                   deleted = true,
                                   content = null
                               )
                           } else comment
                       }
                    )
                }
                else -> s
            }
            _comments.value = next

            val cur = _commentCounts.value[postIdStr] ?: 0
            _commentCounts.value = _commentCounts.value + (postIdStr to (cur)) //댓글 수는 그대로
            onComplete?.invoke()
        }.onFailure { e->
            _comments.value = UiState.Error(e.message ?: "댓글 삭제 실패")
        }
    }

    fun update(
        postId: Long,
        commentId: Long,
        content: String,
        onComplete: (() -> Unit)? = null
    ) = viewModelScope.launch {
        val response = repo.updateComment(postId, commentId, content)

        response.onSuccess { updated ->
            val next = when(val s = _comments.value) {
                is UiState.Success -> UiState.Success(
                    s.data.map { c ->
                        if (c.id == commentId.toString()) c.copy(content = updated.content)
                        else c
                    }
                )
                else -> s
            }
            _comments.value = next
            onComplete?.invoke()
        }.onFailure { e ->
            _comments.value = UiState.Error(e.message ?: "댓글 수정 실패")
        }
    }


}