package com.example.byeoldori.character

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

    var isPanelExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ---------------------------------------
        // 📋 패널 토글 버튼
        // ---------------------------------------
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Button(onClick = { isPanelExpanded = !isPanelExpanded }) {
                Text(if (isPanelExpanded) "📕 컨트롤러 접기" else "📖 컨트롤러 열기")
            }
        }

        // ---------------------------------------
        // 🌟 테스트 컨트롤러 패널
        // ---------------------------------------
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

                    // 1️⃣ 기본 SHOW / HIDE 버튼
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (!isCharacterVisible)
                                Button(onClick = { controller.showCharacter() }) { Text("👀 SHOW") }
                            else {
                                Button(onClick = { controller.hideCharacter() }) { Text("🙈 HIDE") }
                                Button(onClick = { controller.nextCharacter() }) { Text("➡ NEXT") }
                            }
                        }
                    }

                    // 2️⃣ 감정 표현 테스트 버튼 섹션
                    if (isCharacterVisible) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎭 감정 표현 테스트")

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { vm.playIdleMotion() }) { Text("😌 Idle") }
                                    Button(onClick = { vm.playHappyMotion() }) { Text("😊 Happy") }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { vm.playAngryMotion() }) { Text("😠 Angry") }
                                    Button(onClick = { vm.playCryingMotion() }) { Text("😢 Crying") }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { vm.playHerMotion() }) { Text("💃 Her") }
                                    Button(onClick = { vm.playAppearanceMotion() }) { Text("🌟 Appearance") }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { vm.playExitMotion() }) { Text("👋 Exit") }
                                }
                            }
                        }

                        // 3️⃣ 기존 모션/표정 버튼
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { controller.playMotion("TapBody", 0) }) { Text("▶ TapBody") }
                                Button(onClick = { controller.setExpression("f00") }) { Text("😃 표정 f00") }
                            }
                        }

                        // 4️⃣ 말풍선 테스트
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
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { controller.fadeInCharacter() }) { Text("페이드인") }
                                Button(onClick = { controller.fadeOutCharacter() }) { Text("페이드아웃") }
                            }
                        }

                        // 5️⃣ 크기/위치 조정 및 기타
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { controller.resizeBy(50.dp) }) { Text("➕ 커지기") }
                                Button(onClick = { controller.resizeBy((-50).dp) }) { Text("➖ 작아지기") }
                                Button(onClick = { controller.resetSizeAndPosition() }) { Text("🔄 초기화") }
                            }
                        }

                        // 6️⃣ 모션 리스트
                        item { Button(onClick = { controller.refreshMotions() }) { Text("🔄 모션 새로고침") } }
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
                                Text("▶ $motionGroup ($motionIndex)")
                            }
                        }
                    }
                }
            }
        }
    }
}
