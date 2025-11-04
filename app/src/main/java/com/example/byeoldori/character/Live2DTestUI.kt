package com.example.byeoldori.character

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Live2DTestUI(
    vm: Live2DControllerViewModel = hiltViewModel()
) {
    val controller = vm.controller
    val motions by controller.motions.collectAsState()
    val isCharacterVisible by controller.isVisible.collectAsState()

    // 🔹 테스트용 UI 전체를 접고 펼치는 상태값
    var isPanelExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // -------------------------------
        // 📋 좌측 상단: 패널 토글 버튼
        // -------------------------------
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Button(onClick = { isPanelExpanded = !isPanelExpanded }) {
                Text(if (isPanelExpanded) "📕 컨트롤러 접기" else "📖 컨트롤러 열기")
            }
        }

        // -------------------------------
        // 🌟 테스트 컨트롤러 패널 (펼쳐진 상태일 때만)
        // -------------------------------
        if (isPanelExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                tonalElevation = 6.dp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. 최소 UI (SHOW / HIDE)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isCharacterVisible) Arrangement.SpaceBetween else Arrangement.Start
                        ) {
                            if (!isCharacterVisible) {
                                Button(onClick = { controller.showCharacter() }) { Text("👀 SHOW") }
                            } else {
                                Button(onClick = { controller.showCharacter() }) { Text("👀 SHOW") }
                                Button(onClick = { controller.hideCharacter() }) { Text("🙈 HIDE") }
                            }
                        }
                    }

                    // 2. 캐릭터가 보일 때만 확장 UI 표시
                    if (isCharacterVisible) {

                        // 캐릭터 변경
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { controller.nextCharacter() }) { Text("➡ 다음 캐릭터") }
                                Button(onClick = { controller.changeCharacter(0) }) { Text("🔄 캐릭터 0번") }
                            }
                        }

                        // 모션 / 표정
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { controller.playMotion("TapBody", 0) }) { Text("▶ TapBody 모션") }
                                Button(onClick = { controller.setExpression("f00") }) { Text("😃 표정 f00") }
                            }
                        }

                        // 말풍선 위치 테스트
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = {
                                    controller.showSpeech("안녕하세요 🌟", TailPosition.Left, Alignment.TopCenter)
                                }) { Text("💬 좌측") }
                                Button(onClick = {
                                    controller.showSpeech("리겔은 푸른 별 ✨", TailPosition.Center, Alignment.TopCenter)
                                }) { Text("💬 중앙") }
                                Button(onClick = {
                                    controller.showSpeech("베텔게우스 🔥", TailPosition.Right, Alignment.TopCenter)
                                }) { Text("💬 우측") }
                            }
                        }

                        // 텍스트 변경 버튼
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = {
                                    controller.showSpeech("나는 별도리에요", TailPosition.Center, Alignment.BottomCenter)
                                }) { Text("💬 텍스트1") }

                                Button(onClick = {
                                    controller.showSpeech("별보는걸 좋아해요\n같이보러갈래요?", TailPosition.Center, Alignment.BottomCenter)
                                }) { Text("💬 텍스트2") }
                            }
                        }

                        // 크기 변경
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { controller.resizeBy(50.dp) }) { Text("➕ 커지기") }
                                Button(onClick = { controller.resizeBy((-50).dp) }) { Text("➖ 작아지기") }
                            }
                        }

                        // 위치 이동
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { controller.moveBy((-50).dp, 0.dp) }) { Text("⬅ 왼쪽 -50") }
                                Button(onClick = { controller.moveBy(50.dp, 0.dp) }) { Text("➡ 오른쪽 +50") }
                            }
                        }

                        // 위/아래 이동
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { controller.moveBy(0.dp, (-50).dp) }) { Text("⬆ 위로 -50") }
                                Button(onClick = { controller.moveBy(0.dp, 50.dp) }) { Text("⬇ 아래로 +50") }
                            }
                        }

                        // 초기화
                        item {
                            Button(onClick = { controller.resetSizeAndPosition() }) { Text("🔄 초기화") }
                        }

                        // 애니메이션 이동
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(onClick = { controller.animateMoveX(0.5, 300.dp) }) { Text("➡ 애니메이션 이동") }
                                Button(onClick = { controller.animateMoveXEaseOut(0.5, (-300).dp) }) { Text("⬅ EaseOut 이동") }
                            }
                            Button(
                                onClick = { controller.animateCustomSmoothMove(2.0, ((-300).dp), 50.dp) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Smooth 이동")
                            }
                        }

                        // 모션 새로고침
                        item {
                            Button(onClick = { controller.refreshMotions() }) { Text("🔄 모션 새로고침") }
                        }

                        // 모션 리스트
                        items(motions) { fullName ->
                            val parts = fullName.split("_")
                            val motionGroup = parts.getOrNull(0) ?: "Unknown"
                            val motionIndex = parts.getOrNull(1)?.toIntOrNull() ?: 0
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = { controller.playMotion(motionGroup, motionIndex) }
                            ) {
                                Text("▶ $motionGroup ($motionIndex) 모션 실행")
                            }
                        }
                    }
                }
            }
        }
    }
}
