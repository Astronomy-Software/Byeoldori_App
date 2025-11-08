package com.example.byeoldori.character

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.byeoldori.ui.theme.TextHighlight

enum class TailPosition {
    Left, Right, Center
}

/**
 * 💬 CharacterBubbleShape (꼬리 고정)
 * - 꼬리는 고정된 위치에 유지
 * - 말풍선은 폭이 늘어날 때 꼬리 반대 방향으로 이동
 */
class CharacterBubbleShape(
    private val tailPosition: TailPosition,
    private val cornerRadius: Dp = 32.dp,
    private val tailWidth: Dp = 32.dp,
    private val tailHeight: Dp = 16.dp,
    private val tailOffset: Dp = 40.dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val tailWidthPx = with(density) { tailWidth.toPx() }
        val tailHeightPx = with(density) { tailHeight.toPx() }
        val tailOffsetPx = with(density) { tailOffset.toPx() }

        val width = size.width
        val mainBottomY = size.height - tailHeightPx
        val path = Path()

        // ✅ 꼬리는 항상 고정된 기준 위치
        val baseX = when (tailPosition) {
            TailPosition.Left -> tailOffsetPx
            TailPosition.Right -> width - tailOffsetPx
            TailPosition.Center -> width / 2f
        }

        val baseLeftX = baseX - tailWidthPx / 2
        val baseRightX = baseX + tailWidthPx / 2

        // ===============================
        //  Path 그리기
        // ===============================
        path.moveTo(cornerRadiusPx, 0f)

        // 상단 → 오른쪽 위 코너
        path.lineTo(width - cornerRadiusPx, 0f)
        if (cornerRadiusPx > 0) {
            path.arcTo(Rect(width - 2 * cornerRadiusPx, 0f, width, 2 * cornerRadiusPx), 270f, 90f, false)
        }

        // 오른쪽 변
        path.lineTo(width, mainBottomY - cornerRadiusPx)
        if (cornerRadiusPx > 0) {
            path.arcTo(Rect(width - 2 * cornerRadiusPx, mainBottomY - 2 * cornerRadiusPx, width, mainBottomY), 0f, 90f, false)
        }

        // 아래 변 (꼬리 시작 전까지)
        path.lineTo(baseRightX, mainBottomY)

        // 꼬리
        path.lineTo(baseX, size.height)
        path.lineTo(baseLeftX, mainBottomY)

        // 나머지 아래 변
        path.lineTo(cornerRadiusPx, mainBottomY)
        if (cornerRadiusPx > 0) {
            path.arcTo(Rect(0f, mainBottomY - 2 * cornerRadiusPx, 2 * cornerRadiusPx, mainBottomY), 90f, 90f, false)
        }

        // 왼쪽 변
        path.lineTo(0f, cornerRadiusPx)
        if (cornerRadiusPx > 0) {
            path.arcTo(Rect(0f, 0f, 2 * cornerRadiusPx, 2 * cornerRadiusPx), 180f, 90f, false)
        }

        path.close()
        return Outline.Generic(path)
    }
}

@Composable
fun CharacterSpeechBubble(
    text: String,
    tailPosition: TailPosition,
    alignment: Alignment,
    pixelOffset: IntOffset,
    modifier: Modifier = Modifier,
    backgroundColor: Color = TextHighlight.copy(alpha = 0.70f),
    cornerRadius: Dp = 16.dp,
    tailWidth: Dp = 24.dp,
    tailHeight: Dp = 16.dp,
    tailOffset: Dp = 40.dp
) {
    val density = LocalDensity.current
    var bubbleHeightPx by remember { mutableStateOf(0) }
    var bubbleWidthPx by remember { mutableStateOf(0f) }

    // 💡 기준 폭
    val baseWidthPx = with(density) { (120+32).dp.toPx() } // 내용과 패딩 합쳐야함

    // 💡 말풍선 크기 측정
    val modifierWithSize = modifier.onGloballyPositioned { coordinates ->
        bubbleHeightPx = coordinates.size.height
        bubbleWidthPx = coordinates.size.width.toFloat()
    }

    // 💡 폭 차이에 따른 이동 보정값
    val widthDelta = (bubbleWidthPx - baseWidthPx).coerceAtLeast(0f)
    val shiftX = when (tailPosition) {
        TailPosition.Left -> widthDelta / 2f     // 폭이 커질수록 오른쪽으로 이동
        TailPosition.Right -> -widthDelta / 2f    // 폭이 커질수록 왼쪽으로 이동
        TailPosition.Center -> 0f
    }

    val adjustedOffset = remember(pixelOffset, bubbleHeightPx, shiftX) {
        IntOffset(
            pixelOffset.x + shiftX.toInt(),
            pixelOffset.y - (bubbleHeightPx / 2)
        )
    }

    Popup(
        alignment = alignment,
        offset = adjustedOffset,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            clippingEnabled = false,
            excludeFromSystemGesture = true
        )
    ) {
        Box(modifier = modifierWithSize) {
            BubbleContent(
                text = text,
                tailPosition = tailPosition,
                modifier = Modifier,
                backgroundColor = backgroundColor,
                cornerRadius = cornerRadius,
                tailWidth = tailWidth,
                tailHeight = tailHeight,
                tailOffset = tailOffset
            )
        }
    }
}

@Composable
private fun BubbleContent(
    text: String,
    tailPosition: TailPosition,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    cornerRadius: Dp,
    tailWidth: Dp,
    tailHeight: Dp,
    tailOffset: Dp
) {
    val shape = CharacterBubbleShape(tailPosition, cornerRadius, tailWidth, tailHeight, tailOffset)
    val alignment = when (tailPosition) {
        TailPosition.Left -> Alignment.BottomStart
        TailPosition.Center -> Alignment.BottomCenter
        TailPosition.Right -> Alignment.BottomEnd
    }

    Box(
        modifier = modifier
            .wrapContentSize(align = alignment)
            .background(backgroundColor, shape)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = tailHeight + 12.dp)
            .widthIn(min = 120.dp, max = 330.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun SpeechBubbleTestScreen() {
    var inputText by remember { mutableStateOf("말풍선 크기에 따라 이동 테스트!") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFECEFF1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("말풍선 텍스트 입력") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("폭이 늘어나면 꼬리 반대 방향으로 이동합니다.", style = MaterialTheme.typography.bodySmall)
        }

        // 왼쪽 꼬리
        CharacterSpeechBubble(
            text = inputText,
            tailPosition = TailPosition.Left,
            alignment = Alignment.BottomStart,
            pixelOffset = IntOffset(0, 0),
            backgroundColor = Color(0xFF81D4FA).copy(alpha = 0.9f),
            cornerRadius = 16.dp, tailWidth = 24.dp, tailHeight = 16.dp, tailOffset = 40.dp
        )

        // 중앙 꼬리
        CharacterSpeechBubble(
            text = inputText,
            tailPosition = TailPosition.Center,
            alignment = Alignment.Center,
            pixelOffset = IntOffset(0, 0),
            backgroundColor = Color(0xFFFFF176).copy(alpha = 0.9f),
            cornerRadius = 16.dp, tailWidth = 24.dp, tailHeight = 16.dp, tailOffset = 40.dp
        )

        // 오른쪽 꼬리
        CharacterSpeechBubble(
            text = inputText,
            tailPosition = TailPosition.Right,
            alignment = Alignment.TopEnd,
            pixelOffset = IntOffset(0, 0),
            backgroundColor = Color(0xFFA5D6A7).copy(alpha = 0.9f),
            cornerRadius = 16.dp, tailWidth = 24.dp, tailHeight = 16.dp, tailOffset = 40.dp
        )
    }
}
