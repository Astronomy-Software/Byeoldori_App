package com.example.byeoldori.ui.components.community

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.ReviewComment

@Composable
private fun formatCommentTime(createdAt: Long): String = try {
    if (createdAt.toString().length == 12) {
        val sdf = java.text.SimpleDateFormat("yyyyMMddHHmm", java.util.Locale.getDefault())
        val d = sdf.parse(createdAt.toString())
        java.text.SimpleDateFormat("yy.MM.dd  HH:mm", java.util.Locale.getDefault()).format(d!!)
    } else {
        val d = java.util.Date(createdAt)
        java.text.SimpleDateFormat("yy.MM.dd  HH:mm", java.util.Locale.getDefault()).format(d)
    }
} catch (_: Exception) { "--:--" }


@Composable
fun CommentItem(
    comment: ReviewComment,
    onLike: (ReviewComment) -> Unit = {},
    onReply: (ReviewComment) -> Unit = {},
    onEdit: (ReviewComment) -> Unit = {},
    onDelete: (ReviewComment) -> Unit = {},
    canEditDelete: (ReviewComment) -> Boolean = { false },
    isLiked: Boolean = false
) {
    val likeTint by animateColorAsState(
        targetValue = if (isLiked) Purple500 else Color.Unspecified,
    )

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
                        text = comment.author,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = 17.sp,
                        color = TextHighlight
                    )
                    Spacer(Modifier.weight(1f))
                    if(canEditDelete(comment)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "수정",
                                color = TextDisabled,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable { onEdit(comment) }
                            )
                            Text(
                                "삭제",
                                color = TextDisabled,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable { onDelete(comment) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text( //작성 시간
                    text = formatCommentTime(comment.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                    fontSize = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text( //본문
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            color = TextHighlight,
            fontSize = 17.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top=8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(enabled = !isLiked) { onLike(comment) }
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

            Icon(
                painter = painterResource(R.drawable.ic_comment),
                contentDescription = "대댓글",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(17.dp)
                    .clickable{onReply(comment)}
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "${comment.commentCount}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                //modifier = Modifier.alignByBaseline()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860)
@Composable
private fun Preview_CommentItem() {
    MaterialTheme {
        val sample = ReviewComment(
            id = "c1",
            reviewId = "r1",
            author = "아이마카",
            profile = R.drawable.profile1,
            content = "색다른 곳 있으면 알려주세요~",
            likeCount = 3,
            commentCount = 1,
            createdAt = System.currentTimeMillis()
        )
        CommentItem(comment = sample)
    }
}