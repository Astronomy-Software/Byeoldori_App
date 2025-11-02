package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.domain.Community.*
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.theme.*

@Composable
fun SourceBadge(source: CommentSourceType) {
    val (label,bg,fg) = when (source) {
        CommentSourceType.REVIEW -> Triple("관측 후기", Purple500, Color.White)
        CommentSourceType.FREE -> Triple("자유게시판", Purple500, Color.White)
        CommentSourceType.EDUCATION -> Triple("교육 프로그램", Purple500, Color.White)
    }
    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MyComment(
    group: MyCommentGroup,
    myId: Long,
    myNickname: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                group.postTitle,
                color = TextHighlight,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold, fontSize = 25.sp
                )
            )
            Spacer(Modifier.width(10.dp))
            SourceBadge(group.source)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile1),
                contentDescription = "작성자 프로필",
                tint = Color.Unspecified,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    text = group.postAuthorName,
                    color = TextHighlight,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = group.postCreatedAt.toShortDate(),
                    color = TextDisabled,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        group.myComments.forEach { m ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    painter = painterResource(R.drawable.ic_reply),
                    contentDescription = "대댓글 화살표",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .offset(y = 8.dp)
                )
                Spacer(Modifier.width(6.dp))
                CommentItem(
                    comment = ReviewComment(
                        id = m.commentId,
                        reviewId = group.postId,
                        parentId = null,
                        authorId = myId,
                        authorNickname = myNickname,
                        profile = R.drawable.profile1,
                        content = m.content,
                        likeCount = 0,
                        commentCount = 0,
                        createdAt = m.createdAt,
                        liked = false,
                        deleted = false
                    ),
                    showCommentCount = false,
                    showActions = false
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 2.dp)
        Spacer(Modifier.height(10.dp))
    }
}