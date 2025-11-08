package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.ui.theme.Blue800
import com.example.byeoldori.ui.theme.ErrorRed
import java.lang.Error

data class MenuItem(
    val title: String,
    val onClick: () -> Unit = {},
    val trailing: (@Composable () -> Unit)? = null, // 스위치, 텍스트 등
    val color: Color = Color.White
)

@Composable
fun MenuGroupCard(
    containerColor: Color,
    items: List<MenuItem>
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    Surface(color = containerColor,
        shape = shape,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), shape)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items.forEachIndexed { i, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .clickable { item.onClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.title,
                        color = item.color,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if(item.trailing != null) {
                        item.trailing.invoke()
                    } else {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                if (i != items.lastIndex) Divider(color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2B184F, widthDp = 360)
@Composable
private fun PreviewMenuGroupCard() {
    MenuGroupCard(
        containerColor = Blue800,
        items = listOf(
            MenuItem("관측 일정 및 나의 관측 후기",color = Color.White),
            MenuItem("찜",color = Color.White) ,
            MenuItem("좋아요",color = Color.White) ,
            MenuItem("내가 작성한 자유게시글",color = Color.White),
            MenuItem("내가 작성한 교육 프로그램",color = Color.White),
            MenuItem("내가 작성한 댓글",color = Color.White),
            MenuItem("고객 센터",color = Color.White),
            MenuItem("로그아웃", color = ErrorRed), // 노랑
            MenuItem("회원 탈퇴", color = ErrorRed)
        )
    )
}