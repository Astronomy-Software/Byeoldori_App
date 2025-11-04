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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.TextHighlight


enum class TailPosition {
    Left,   // 왼쪽 말풍선 (고정 오프셋)
    Right,  // 오른쪽 말풍선 (고정 오프셋)
    Center  // 중앙 말풍선
}

class CharacterBubbleShape(
    private val tailPosition: TailPosition,
    private val cornerRadius: Dp = 32.dp,
    private val tailWidth: Dp = 32.dp,
    private val tailHeight: Dp = 16.dp,
    private val tailOffset: Dp = 40.dp // ← 꼬리 위치 고정 오프셋
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val tailWidthPx = with(density) { tailWidth.toPx() }
        val tailHeightPx = with(density) { tailHeight.toPx() }
        val tailOffsetPx = with(density) { tailOffset.toPx() }

        val width = size.width
        val mainBottomY = size.height - tailHeightPx
        val path = Path()

        // 꼬리 base 위치 계산
        val baseX = when (tailPosition) {
            TailPosition.Left -> tailOffsetPx
            TailPosition.Right -> width - tailOffsetPx
            TailPosition.Center -> width / 2f
        }
        val baseLeftX = baseX - tailWidthPx / 2
        val baseRightX = baseX + tailWidthPx / 2

        // 말풍선 Path
        path.moveTo(cornerRadiusPx, 0f)

        // 상단 → 오른쪽 위 코너
        path.lineTo(width - cornerRadiusPx, 0f)
        if (cornerRadiusPx > 0) {
            path.arcTo(
                Rect(width - 2 * cornerRadiusPx, 0f, width, 2 * cornerRadiusPx),
                270f, 90f, false
            )
        }

        // 오른쪽 변 → 오른쪽 아래
        path.lineTo(width, mainBottomY - cornerRadiusPx)
        if (cornerRadiusPx > 0) {
            path.arcTo(
                Rect(width - 2 * cornerRadiusPx, mainBottomY - 2 * cornerRadiusPx, width, mainBottomY),
                0f, 90f, false
            )
        }

        // 아래 변 (꼬리 전까지)
        path.lineTo(baseRightX, mainBottomY)

        // 꼬리 삼각형
        path.lineTo(baseX, size.height)
        path.lineTo(baseLeftX, mainBottomY)

        // 나머지 아래변
        path.lineTo(cornerRadiusPx, mainBottomY)
        if (cornerRadiusPx > 0) {
            path.arcTo(
                Rect(0f, mainBottomY - 2 * cornerRadiusPx, 2 * cornerRadiusPx, mainBottomY),
                90f, 90f, false
            )
        }

        // 왼쪽 변
        path.lineTo(0f, cornerRadiusPx)
        if (cornerRadiusPx > 0) {
            path.arcTo(
                Rect(0f, 0f, 2 * cornerRadiusPx, 2 * cornerRadiusPx),
                180f, 90f, false
            )
        }

        path.close()
        return Outline.Generic(path)
    }
}

// 팝업을 생성하고 BubbleContent를 띄우는 메인 Composable
@Composable
fun CharacterSpeechBubble(
    text: String,
    tailPosition: TailPosition,

    // Live2DScreen에서 전달받을 위치 파라미터를 추가
    alignment: Alignment,
    pixelOffset: IntOffset,

    modifier: Modifier = Modifier,
    backgroundColor: Color = TextHighlight.copy(alpha = 0.70f),
    cornerRadius: Dp = 16.dp,
    tailWidth: Dp = 24.dp,
    tailHeight: Dp = 16.dp,
    tailOffset: Dp = 40.dp // ← 꼬리 위치 고정
) {
    // Popup을 여기서 생성합니다.
    Popup(
        alignment = alignment,
        offset = pixelOffset,
        // 팝업 외부를 클릭해도 닫히지 않도록 focusable false 설정
        properties = PopupProperties(focusable = false)
    ) {
        // 실제 말풍선 UI를 렌더링
        BubbleContent(
            text = text,
            tailPosition = tailPosition,
            modifier = modifier,
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            tailWidth = tailWidth,
            tailHeight = tailHeight,
            tailOffset = tailOffset
        )
    }
}

// 실제 말풍선 UI 내용을 렌더링하는 내부 Composable 함수
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

    // 이 Box가 실제 팝업의 콘텐츠 뷰가 됩니다.
    Box(
        modifier = modifier
            .wrapContentSize(align = alignment)
            .background(backgroundColor, shape)
            // 꼬리 높이만큼 하단 패딩 추가
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = tailHeight + 12.dp)
            .widthIn(min = 120.dp, max = 280.dp) // 범위 제한
    ) {
        // 텍스트 색상을 TextHighlight로 지정
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun EditableSpeechBubbleDemo() {
    var text by remember { mutableStateOf("수정 가능한 말풍선!") }
    Background{
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("텍스트 입력") }
            )
            // Preview에서는 Popup 없이 BubbleContent 직접 호출
            BubbleContent(text, TailPosition.Left, Modifier ,Color(0xFFFFFFFF).copy(alpha=0.5f), 16.dp, 24.dp, 16.dp, 40.dp)
            BubbleContent(text, TailPosition.Center, Modifier ,Color(0xFFFFF176).copy(alpha=0.8f), 16.dp, 24.dp, 16.dp, 40.dp)
            BubbleContent(text, TailPosition.Right, Modifier, Color(0xFFA5D6A7).copy(alpha=0.8f), 16.dp, 24.dp, 16.dp, 40.dp)
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun SpeechBubbleTestScreen() {
    var inputText by remember { mutableStateOf("여기에 텍스트 입력!") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFECEFF1)) // 테스트용 배경
    ) {
        // 입력창 (상단 고정)
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
            Text("입력한 텍스트가 아래 말풍선에 반영됩니다!", style = MaterialTheme.typography.bodySmall)
        }

        // 왼쪽 꼬리 → 화면 왼쪽 하단
        BubbleContent(
            text = inputText,
            tailPosition = TailPosition.Left,
            backgroundColor = Color(0xFF81D4FA).copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            cornerRadius = 16.dp, tailWidth = 24.dp, tailHeight = 16.dp, tailOffset = 40.dp
        )

        // 중앙 꼬리 → 화면 중앙
        BubbleContent(
            text = inputText,
            tailPosition = TailPosition.Center,
            backgroundColor = Color(0xFFFFF176).copy(alpha = 0.9f),
            modifier = Modifier.align(Alignment.Center),
            cornerRadius = 16.dp, tailWidth = 24.dp, tailHeight = 16.dp, tailOffset = 40.dp
        )

        // 오른쪽 꼬리 → 화면 오른쪽 하단
        BubbleContent(
            text = inputText,
            tailPosition = TailPosition.Right,
            backgroundColor = Color(0xFFA5D6A7).copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            cornerRadius = 16.dp, tailWidth = 24.dp, tailHeight = 16.dp, tailOffset = 40.dp
        )
    }
}