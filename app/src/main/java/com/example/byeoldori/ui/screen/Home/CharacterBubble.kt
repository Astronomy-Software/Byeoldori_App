package com.example.byeoldori.ui.screen.Home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CharacterBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 말풍선 본체
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // 꼬리 (Canvas 삼각형)
        Canvas(
            modifier = Modifier
                .width(20.dp)
                .height(12.dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height) // 아래쪽 중앙
                lineTo(0f, 0f) // 왼쪽 위
                lineTo(size.width, 0f) // 오른쪽 위
                close()
            }
            drawPath(path, color = Color.White)
            drawPath(path, color = Color.Black, style = Stroke(width = 2f)) // 테두리
        }
    }
}

@Preview
@Composable
fun BubblePreviewScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CharacterBubble("오리온자리를 맨눈으로 관측하는건 좋은 선택이에요")
    }
}
