package com.example.byeoldori.ui.components.community.freeboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Community.FreePost
import com.example.byeoldori.domain.Content

@Composable
fun FreeBoardItem( //게시글 UI
    post: FreePost,
    likeCount: Int = post.likeCount,
    commentCount: Int,
    isLiked: Boolean = post.liked,
    onClick: () -> Unit = {},
    onLikeClick: () -> Unit
) {
   // val isLiked by remember { derivedStateOf { likedKeyFree(post.id) in FreeBoardState.likedPostIds } }
   // val isLiked by remember { derivedStateOf { likedKeyFree(post.id) in LikeState.ids } }
    val likeKey = remember(post.id) { likedKeyFree(post.id) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(text = post.title, fontSize = 17.sp, color = Color.White)
            Spacer(Modifier.height(10.dp))
            val previewText = remember(post.contentItems) {
                post.contentItems.firstOrNull { it is Content.Text }
                    ?.let { (it as Content.Text).text }
                    ?: ""
            }
            if (previewText.isNotBlank()) {
                Text(
                    text = previewText,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(10.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.profile1),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(post.author, fontSize = 15.sp, color = TextHighlight)

                Spacer(Modifier.weight(1f)) //빈공간을 늘림

                Row(
                    modifier = Modifier
                        .clickable {
                            //로컬(전역) 좋아요 상태를 먼저 토글 (아이콘 색 즉시 반응)
                            LikeState.ids = if (isLiked) {
                                LikeState.ids - likeKey
                            } else {
                                LikeState.ids + likeKey
                            }
                            //서버 갱신 호출
                            onLikeClick()
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_thumbs_up),
                        contentDescription = null,
                        tint = if (isLiked) Purple500 else Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text("$likeCount", fontSize = 15.sp, color = Color.White)
                }

                Spacer(Modifier.width(8.dp))

                Icon(
                    painter = painterResource(R.drawable.ic_comment),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text("$commentCount", fontSize = 15.sp, color = Color.White)
            }
        }
    }
}

@Preview(
    name = "FreeBoardItem",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_FreeBoardItem() {
    val sample = FreePost(
        id = "p1",
        title = "처음 뵙겠습니다!",
        author = "아이마카",
        likeCount = 21,
        commentCount = 21,
        viewCount = 120,
        createdAt = "25.10.30",
        contentItems = listOf(
            Content.Text("오늘 처음 가입했습니다."),
            Content.Image.Resource(R.drawable.img_dummy),
            Content.Text("좋은 관측 장소 공유 부탁드려요!")
        ),
        profile = R.drawable.profile1,
        liked = false
    )
    MaterialTheme {
        Surface(color = Blue800) {
            FreeBoardItem(post = sample, onClick = {}, commentCount = 21, onLikeClick = {}, isLiked = false)
        }
    }
}