package com.example.byeoldori.character

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp) // 항목 간격
    ) {
        // ✅ 캐릭터 보이기/숨기기
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { controller.showCharacter() }) { Text("👀 SHOW") }
                Button(onClick = { controller.hideCharacter() }) { Text("🙈 HIDE") }
            }
        }
        // 캐릭터 변경 / 모션
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { controller.nextCharacter() }) { Text("➡ 다음 캐릭터") }
                Button(onClick = { controller.changeCharacter(0) }) { Text("🔄 캐릭터 0번") }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { controller.playMotion("TapBody", 0) }) { Text("▶ TapBody 모션") }
                Button(onClick = { controller.setExpression("f00") }) { Text("😃 표정 f00") }
            }
        }

        // 말풍선 위치별 테스트
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

        // 텍스트 변경 버튼들
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

        // 크기 변경 버튼
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = {
                    controller.updateModifier(Modifier.size(300.dp, 200.dp))
                }) { Text("📏 300x200") }

                Button(onClick = {
                    controller.updateModifier(Modifier.size(400.dp, 300.dp))
                }) { Text("📏 400x300") }
            }
        }

        // ✅ 위치(offset) 변경 버튼
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = {
                    controller.updateModifier(
                        Modifier
                            .size(300.dp, 200.dp)
                            .offset(x = (-50).dp, y = 0.dp)
                    )
                }) { Text("⬅ 왼쪽 -50") }

                Button(onClick = {
                    controller.updateModifier(
                        Modifier
                            .size(300.dp, 200.dp)
                            .offset(x = 50.dp, y = 0.dp)
                    )
                }) { Text("➡ 오른쪽 +50") }
            }
        }

        // ✅ 위/아래 이동 버튼 추가
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = {
                    controller.updateModifier(
                        Modifier
                            .size(300.dp, 200.dp)
                            .offset(x = 0.dp, y = (-50).dp)
                    )
                }) { Text("⬆ 위로 -50") }

                Button(onClick = {
                    controller.updateModifier(
                        Modifier
                            .size(300.dp, 200.dp)
                            .offset(x = 0.dp, y = 50.dp)
                    )
                }) { Text("⬇ 아래로 +50") }
            }
        }

        // 모션 새로고침
        item {
            Button(onClick = { controller.refreshMotions() }) { Text("🔄 모션 새로고침") }
        }

        // ✅ 모션 버튼 목록 → 항상 맨 밑에
        items(motions) { motion ->
            val (group, idx) = motion.split(":")
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                onClick = { controller.playMotion(group, idx.toIntOrNull() ?: 0) }
            ) {
                Text("▶ $motion 실행")
            }
        }
    }
}
