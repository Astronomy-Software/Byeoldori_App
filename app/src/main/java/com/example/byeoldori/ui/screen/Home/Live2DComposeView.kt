package com.example.byeoldori.ui.screen.live2d

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.live2d.live2dview.Live2DView

@Composable
fun Live2DScreen() {
    val context = LocalContext.current
    var live2DView: Live2DView? by remember { mutableStateOf(null) }
    var motions by remember { mutableStateOf(listOf<String>()) }

    // 대화형 말풍선 상태
    var speech by remember { mutableStateOf("오늘은 어떤 별을 관측해볼까?") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Live2DView (GLSurfaceView)
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            factory = { ctx: Context ->
                Live2DView(ctx).apply {
                    live2DView = this
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // ───── 기본 제어 버튼들 ─────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { live2DView?.nextCharacter() }) {
                Text("➡ 다음 캐릭터")
            }
            Button(onClick = { live2DView?.changeCharacter(0) }) {
                Text("🔄 캐릭터 0번으로")
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { live2DView?.playMotion("TapBody", 0) }) {
                Text("▶ TapBody 모션")
            }
            Button(onClick = { live2DView?.setExpression("f00") }) {
                Text("😃 표정 변경 (f00)")
            }
        }

        // 대화형 말풍선 (상태 기반)
        CharacterBubble(
            text = speech,
        )


        Spacer(Modifier.height(16.dp))

        // ───── 모션 목록 + Refresh 버튼 ─────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("📂 모션 목록")
            Button(onClick = {
                motions = live2DView?.getAvailableMotions() ?: emptyList()
            }) {
                Text("🔄 새로고침")
            }
        }

        Spacer(Modifier.height(8.dp))

        Column {
            motions.forEach { motion ->
                val (group, index) = motion.split(":")
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { live2DView?.playMotion(group, index.toInt()) }
                ) {
                    Text("▶ $motion 실행")
                }
            }
        }
    }
}

@Composable
fun CharacterBubble(text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 말풍선 본체
        androidx.compose.material3.Card(
            modifier = Modifier.wrapContentSize(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        }

        // 꼬리 (▼)
        Text("▼", modifier = Modifier.padding(top = 4.dp))
    }
}

