package com.example.byeoldori.ui.components.community

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import com.example.byeoldori.domain.Community.ReviewComment

//여기서는 댓글 추가와 대댓글 관리
@Composable
fun CommentList(
    postId: String,
    currentUserId: Long?,
    currentUserNickname: String?,
    comments: List<ReviewComment>,
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

    val isMine: (ReviewComment) -> Boolean = remember {
        { c ->
            //임시로 모든 댓글을 "내 댓글"로 처리
            // return true

            // 또는 authorId==2인 경우만 내 댓글로 처리 (테스트용)
            val result = (c.authorId == 2L)
            Log.d("CommentCheck", "comment(${c.id}) ▶ authorId=${c.authorId}, isMine=$result (임시)")
            result
        }
    }


    LaunchedEffect(comments, currentUserId, currentUserNickname) {
        Log.d("CommentCheck", "현재 사용자 id=$currentUserId, nickname=$currentUserNickname")
        comments.forEach { c ->
            val byId = currentUserId != null && c.authorId == currentUserId
            val byNick = !c.authorNickname.isNullOrBlank() &&
                    !currentUserNickname.isNullOrBlank() &&
                    c.authorNickname!!.trim().equals(currentUserNickname!!.trim(), ignoreCase = true)

            Log.d(
                "CommentCheck",
                "comment(${c.id}) ▶ authorId=${c.authorId}, authorNick=${c.authorNickname}, " +
                        "byId=$byId, byNick=$byNick, 최종=${byId || byNick}"
            )
        }
    }


    Column(modifier = Modifier.fillMaxWidth()) {
        //댓글 관리
        val parents = comments.filter { it.parentId == null && it.reviewId == postId }
        parents.forEach { p ->
            val isParentLiked = p.id in liked
            CommentItem(
                comment = p,
                isLiked = p.liked,
                onLike = { onLike(p) },
                onReply = { target -> //대댓글 추가
                    parent = target
                    onReply(target)
                    requestKeyboard = true
                },
                onEdit = { target ->
                    if (isMine(target)) {
                        editingTarget = target
                        input = ""
                        onEdit(target)
                        requestKeyboard = true
                    }
                },
                onDelete = { del ->
                    if (isMine(del)) onDelete(del)
                },
                canEditDelete = isMine //수정/삭제 버튼은 현재 사용자만 보여야 함
            )

            //대댓글 관리
            val replies = comments.filter { it.reviewId == postId && it.parentId == p.id }
            replies.forEach { reply ->
                CommentReplyItem(
                    comment = reply,
                    onLike = { tapped ->
                        val isAdding = tapped.id !in liked
                        val nextLiked = if (isAdding) liked + tapped.id else liked - tapped.id

                        onLikedChange(nextLiked)
                        onLike(tapped)
                    },
                    isLiked = reply.id in liked,
                    onReply = { target ->
                        parent = target
                        onReply(target)
                        requestKeyboard = true
                    },
                    onEdit = { target ->
                        if (isMine(target)) {
                            editingTarget = target
                            input = target.content
                            onEdit(target)
                            requestKeyboard = true
                        }
                    },
                    onDelete = { del ->
                        if (isMine(del)) onDelete(del)
                    },
                    canEditDelete = isMine
                )
            }
        }
    }
}