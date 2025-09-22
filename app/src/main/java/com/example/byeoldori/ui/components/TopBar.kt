package com.example.byeoldori.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.TextHighlight

@Composable
fun TopBar(
    title: String,
    onBack: () -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // 제목
            Text(
                text = title,
                color = TextHighlight,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // 뒤로가기 버튼
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(30.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_before),
                    contentDescription = "뒤로가기",
                    tint = TextHighlight,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Divider( // 🔥 하단 줄
            color = TextHighlight.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}



@Preview(showBackground = true)
@Composable
private fun Preview_TopBar() {
    Background {
        TopBar(
            title = "글쓰기 창",
            onBack = {},
        )
    }
}
