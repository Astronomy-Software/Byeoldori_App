package com.example.byeoldori.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun Background(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 기본 배경
        Canvas(Modifier.fillMaxSize()) {
            val end = Offset(size.width, size.height) // 좌상단 → 우하단 대각
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Blue800, Purple800),
                    start = Offset.Zero,
                    end = end
                )
            )
        }

        // 별 레이어
        StarTwinkleLayer(isAnimating = isAnimating)
        // 만약 꾸미고 싶을 경우 레이어 추가.
        // 콘텐츠 슬롯
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { content() }
    }
}

@Composable
private fun StarTwinkleLayer(isAnimating: Boolean) {
    val infinite = rememberInfiniteTransition(label = "stars")
    val twinkle by infinite.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    val stars = remember {
        List(80) {
            Offset(
                x = Random.nextFloat(),            // 0..1 (폭 비율)
                y = Random.nextFloat() * 0.65f     // 0..0.65 (상단에 몰리도록)
            )
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        stars.forEach { star ->
            val r = (0.8f + Random.nextFloat() * 1.3f) * (if (isAnimating) twinkle else 1f)
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = r,
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewStarBackground_Night() {
    Background(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("🌌 Starry Night Preview", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewStarBackground_OnlyText() {
    Background(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("별만 있는 배경", modifier = Modifier.padding(16.dp))
    }
}
