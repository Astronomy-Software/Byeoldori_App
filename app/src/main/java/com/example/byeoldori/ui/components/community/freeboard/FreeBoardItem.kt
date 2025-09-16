package com.example.byeoldori.ui.components.community.freeboard

import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.FreePost

@Composable
fun FreeBoardItem(
    post: FreePost,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(text = post.title, fontSize = 17.sp, color = Color.White)
            Spacer(Modifier.height(10.dp))
            Text(
                text = post.content,
                fontSize = 15.sp,
                color = Color.White,
                maxLines = 2,
                style = LocalTextStyle.current.copy(
                    lineHeight = 20.sp
                )
            )
            Spacer(Modifier.height(10.dp))

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

                Icon(
                    painter = painterResource(R.drawable.ic_thumbs_up),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text("${post.likeCount}", fontSize = 15.sp, color = Color.White)

                Spacer(Modifier.width(8.dp))

                Icon(
                    painter = painterResource(R.drawable.ic_comment),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text("${post.commentCount}", fontSize = 15.sp, color = Color.White)
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
        imageRes = R.drawable.profile1,
        createdAt = 202510301500,
        content = "안녕하세요 오늘 처음 별도리앱 깔았어요\n혹시 다들 어디서 관측하시나요??\n이런 곳은 어떤가요?? 잘보이나요?\"\n",
    )
    MaterialTheme {
        Surface(color = Blue800) {
            FreeBoardItem(post = sample, onClick = {})
        }
    }
}
