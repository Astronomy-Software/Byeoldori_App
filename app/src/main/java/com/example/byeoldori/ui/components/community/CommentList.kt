package com.example.byeoldori.ui.components.community

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import com.example.byeoldori.domain.Community.ReviewComment

//여기서는 댓글 추가와 대댓글 관리
@Composable
fun CommentList(
    postId: String,
    currentUser: String,
    comments: MutableList<ReviewComment>,
    onLike: (ReviewComment) -> Unit,
    onReply: (ReviewComment) -> Unit,
    onEdit: (ReviewComment) -> Unit,
    onDelete: (ReviewComment) -> Unit,
    liked: Set<String>, //사용자가 좋아요를 누른 댓글들의 id값의 집합
    onLikedChange: (Set<String>) -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    var parent by remember { mutableStateOf<ReviewComment?>(null) }
    var editingTarget by remember { mutableStateOf<ReviewComment?>(null) }
    var requestKeyboard by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        //댓글 관리
        val parents = comments.filter { it.parentId == null && it.reviewId == postId }
        parents.forEach { p ->
            CommentItem(
                comment = p,
                isLiked = likedKeyProgramComment(p.id) in LikeState.ids, //p.id가 liked에 해당 댓글이 있으면 true
                onLike = { tapped ->
                    val key = likedKeyProgramComment(tapped.id)
                    val isAdding = tapped.id !in liked //이번 클릭이 추가인지 취소인지

                    LikeState.ids = if (isAdding) LikeState.ids + key else LikeState.ids - key
                    val nextLiked = if (isAdding) liked + tapped.id else liked - tapped.id
                    onLikedChange(nextLiked)

                    val idx = comments.indexOfFirst { it.id == tapped.id }
                    if (idx >= 0) {
                        val cur = comments[idx]
                        comments[idx] = cur.copy(likeCount = (cur.likeCount + if (isAdding) 1 else -1).coerceAtLeast(0))
                    }
                    onLike(tapped)
                },
                onReply = { target -> //대댓글 추가
                    parent = target
                    onReply(target)
                    requestKeyboard = true
                },
                onEdit = { target -> //수정
                    if (target.author == currentUser) {
                        editingTarget = target        // 수정 모드
                        input = ""
                        onEdit(target)
                        requestKeyboard = true
                    }
                },
                onDelete = { del ->
                    val idx = comments.indexOfFirst { it.id == del.id }
                    if (idx >= 0) comments.removeAt(idx)
                },
                canEditDelete = { it.author == currentUser } //수정/삭제 버튼은 현재 사용자만 보여야 함
            )

            //대댓글 관리
            val replies = comments.filter { it.reviewId == postId && it.parentId == p.id }
            replies.forEach { reply ->
                CommentReplyItem(
                    comment = reply,
                    onLike = { tapped ->
                        val key = likedKeyProgramComment(tapped.id)
                        val isAdding = tapped.id !in liked

                        LikeState.ids = if (isAdding) LikeState.ids + key else LikeState.ids - key

                        val nextLiked = if (isAdding) liked + tapped.id else liked - tapped.id
                        onLikedChange(nextLiked)

                        val idx = comments.indexOfFirst { it.id == tapped.id }
                        if (idx >= 0) {
                            val cur = comments[idx]
                            comments[idx] = cur.copy(
                                likeCount = (cur.likeCount + if (isAdding) 1 else -1).coerceAtLeast(0)
                            )
                        }
                        onLike(tapped)
                    },
                    isLiked = reply.id in liked,
                    onReply = { target ->
                        parent = target
                        onReply(target)
                        requestKeyboard = true
                    },
                    onEdit = { target ->
                        if (target.author == currentUser) {
                            editingTarget = target
                            input = target.content
                            onEdit(target)
                            requestKeyboard = true
                        }
                    },
                    onDelete = { del ->
                        val idx = comments.indexOfFirst { it.id == del.id }
                        if (idx >= 0) comments.removeAt(idx)
                        onDelete(del)
                    },
                    canEditDelete = { it.author == currentUser }
                )
            }
        }
    }
}