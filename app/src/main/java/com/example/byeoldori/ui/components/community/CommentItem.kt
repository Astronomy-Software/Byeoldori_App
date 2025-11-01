package com.example.byeoldori.ui.components.community

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Community.ReviewComment
import com.example.byeoldori.viewmodel.dummyFreeComments
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

@Composable
fun CommentItem(
    comment: ReviewComment,
    onLike: (ReviewComment) -> Unit = {},
    onReply: (ReviewComment) -> Unit = {},
    onEdit: (ReviewComment) -> Unit = {},
    onDelete: (ReviewComment) -> Unit = {},
    canEditDelete: (ReviewComment) -> Boolean = { false },
    isLiked: Boolean = false,
    showCommentCount: Boolean = true,
    editingId: Long? = null,
    onSubmitEdit: (String) -> Unit = {},
    onCancelEdit: () -> Unit = {},
    showActions: Boolean = true
) {
    val likeTint by animateColorAsState(
        targetValue = if (isLiked) Purple500 else Color.Unspecified,
    )
    val disabled = comment.deleted

    val isEditing = editingId == comment.id
    val focusRequester = remember { FocusRequester() }
    var editText by remember(comment.id) { mutableStateOf(comment.content.orEmpty())}

    LaunchedEffect(isEditing) {
        if(isEditing) { focusRequester.requestFocus() }
    }

    if(disabled) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 20.dp)
        ){
            Text(
                text = "삭제된 댓글입니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDisabled,
                fontSize = 17.sp
            )
        }
        return //나머지 UI 렌더링 차단
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val profile = comment.profile ?: R.drawable.profile1
            Icon(
                painter = painterResource(profile),
                contentDescription = "프로필 이미지",
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text( //작성자
                        text = comment.authorNickname?.takeIf { it.isNotBlank() } ?: "익명",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = 17.sp,
                        color = TextHighlight
                    )
                    Spacer(Modifier.weight(1f))

                    val canEdit = canEditDelete(comment)
                    if(canEdit && !isEditing) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "수정",
                                color = TextDisabled,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable(enabled = canEdit) {
                                    onEdit(comment)
                                }
                            )
                            Text(
                                "삭제",
                                color = TextDisabled,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable(enabled = canEdit) {
                                    onDelete(comment)
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text( //작성 시간
                    text = comment.createdAt,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                    fontSize = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))

        if(isEditing) {
            OutlinedTextField(
                value = editText,
                onValueChange = { editText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 17.sp, color = TextHighlight),
                placeholder = { Text("내용을 입력해주세요", color = TextDisabled.copy(alpha = 0.4f)) }
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { onCancelEdit() }) { Text("취소") }
                Button(onClick = { onSubmitEdit(editText.trim()) }, enabled = editText.isNotBlank()) {
                    Text("저장")
                }
            }
        } else {
            Text( //본문
                text = comment.content ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                color = TextHighlight,
                fontSize = 17.sp
            )
            if(showActions) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onLike(comment) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_thumbs_up),
                            contentDescription = "좋아요",
                            tint = likeTint,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${comment.likeCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHighlight,
                            modifier = Modifier.alignByBaseline() //베이스라인 맞추기
                        )
                    }
                    Spacer(Modifier.width(12.dp))

                    if (showCommentCount) { //대댓글에 댓글 수를 표시할 필요가 없으니까
                        Icon(
                            painter = painterResource(R.drawable.ic_comment),
                            contentDescription = "대댓글",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(17.dp)
                                .clickable { onReply(comment) }
                        )
                        Spacer(Modifier.width(8.dp))
                        val replyCount = dummyFreeComments.count { it.parentId == comment.id }
                        Text(
                            "$replyCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentReplyItem(
    comment: ReviewComment,
    onLike: (ReviewComment) -> Unit = {},
    onReply: (ReviewComment) -> Unit = {},
    onEdit: (ReviewComment) -> Unit = {},
    onDelete: (ReviewComment) -> Unit = {},
    canEditDelete: (ReviewComment) -> Boolean = { false },
    isLiked: Boolean = false,
    editingId: Long? = null,
    onSubmitEdit: (String) -> Unit = {},
    onCancelEdit: () -> Unit = {}

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_reply),
            contentDescription = "대댓글 화살표",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(30.dp)
                .offset(y=10.dp)
        )
        Spacer(Modifier.width(8.dp))

        if(comment.deleted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "삭제된 댓글입니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDisabled,
                    fontSize = 17.sp
                )
            }
        } else {
            CommentItem(
                comment = comment,
                onLike = onLike,
                onReply = onReply,
                onEdit = onEdit,
                onDelete = onDelete,
                canEditDelete = canEditDelete,
                isLiked = isLiked,
                showCommentCount = false,
                editingId = editingId,
                onSubmitEdit = onSubmitEdit,
                onCancelEdit = onCancelEdit
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860)
@Composable
private fun Preview_CommentItem() {
    MaterialTheme {
        val sample = ReviewComment(
            id = 1,
            reviewId = 2,
            authorId = 123,
            profile = R.drawable.profile1,
            content = "색다른 곳 있으면 알려주세요~",
            likeCount = 3,
            commentCount = 1,
            createdAt = "202510271145",
            parentId = null,
            authorNickname = "아이마카",
            liked = true
        )
        CommentItem(comment = sample)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860)
@Composable
private fun Preview_ReplyItem() {
    MaterialTheme {
        val reply = ReviewComment(
            id = 2,
            reviewId = 1,
            authorId = 123,
            profile = R.drawable.profile1,
            content = "저도 궁금합니다!",
            likeCount = 1,
            commentCount = 0,
            createdAt = "202510251145",
            parentId = 1, // 부모 댓글 id
            authorNickname = "astro_user",
            liked = false
        )
        CommentReplyItem(comment = reply)
    }
}
