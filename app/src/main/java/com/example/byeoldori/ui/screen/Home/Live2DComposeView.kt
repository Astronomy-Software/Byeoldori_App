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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.live2d.live2dview.Live2DView

@Composable
fun Live2DScreen() {
    val context = LocalContext.current

    // Live2DView 인스턴스를 기억 (외부 제어를 위해)
    var live2DView: Live2DView? by remember { mutableStateOf(null) }

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

        // 캐릭터 제어 버튼들
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
    }
}
