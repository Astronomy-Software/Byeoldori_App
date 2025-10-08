package com.example.byeoldori.ui.components.community

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*

object LikeState {
    // key 규칙: "free:<postId>", "review:<reviewId>" 처럼 prefix로 구분
    var ids by mutableStateOf(setOf<String>()) //사용자가 좋아요를 누른 항목들의 집합
}

//키 생성 함수들
fun likedKeyFree(postId: String) = "free:$postId"
fun likedKeyFreeComment(commentId: String) = "freeComment:$commentId"
fun likedKeyReview(reviewId: String) = "review:$reviewId"
fun likedKeyReviewComment(commentId: String) = "reviewComment:$commentId"
fun likedKeyProgram(programId: String) = "program:$programId"
fun likedKeyProgramComment(commentId: String) = "programComment:$commentId"

@Composable
fun LikeCommentBar (
    key: String,
    likeCount: Int,
    liked: Boolean, //외부에서 주입
    onToggle: () -> Unit,
    onSyncLikeCount: (Int) -> Unit,
    commentCount: Int,
) {
    Column() {
        Divider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //좋아요
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onToggle() }) {
                Icon(
                        painter = painterResource(R.drawable.ic_thumbs_up),
                        contentDescription = "좋아요",
                        tint = if (liked) Purple500 else Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(text = "좋아요   $likeCount", color = TextHighlight, fontSize = 14.sp)
            }
            Divider(
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .height(20.dp)
                    .width(2.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically)
            {
                Icon(
                    painter = painterResource(R.drawable.ic_comment),
                    contentDescription = "댓글",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(text = "댓글   $commentCount", color = Color.White, fontSize = 14.sp)
            }
        }
        Divider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860)
@Composable
fun Preview_LikeCommentBar() {
    MaterialTheme {
        LikeCommentBar(
            key = likedKeyFree("post1"),
            likeCount = 21,
            liked = true,
            onToggle = {},
            onSyncLikeCount = {},
            commentCount = 21
        )
    }
}