package com.example.byeoldori.ui.screen.Home

import CharacterSpeechBubble
import TailPosition
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import com.live2d.live2dview.Live2DView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class Live2DController {
    private var live2DView: Live2DView? = null

    fun attachView(view: Live2DView) {
        live2DView = view
    }

    fun detach() {
        // 필요시 OpenGL/Live2D 리소스 해제 코드 추가
        live2DView = null
    }

    // 말풍선 상태
    private val _speech = MutableStateFlow("오늘은 어떤 별을 관측해볼까?")
    val speech: StateFlow<String> get() = _speech

    private val _tailPosition = MutableStateFlow(TailPosition.Left)
    val tailPosition: StateFlow<TailPosition> get() = _tailPosition

    private val _alignment = MutableStateFlow(Alignment.BottomCenter)
    val alignment: StateFlow<Alignment> get() = _alignment

    // 모션 목록 상태
    private val _motions = MutableStateFlow<List<String>>(emptyList())
    val motions: StateFlow<List<String>> get() = _motions

    // ───── Live2D 기본 제어 ─────
    fun nextCharacter() = live2DView?.nextCharacter()
    fun changeCharacter(index: Int) = live2DView?.changeCharacter(index)
    fun playMotion(group: String, index: Int) = live2DView?.playMotion(group, index)
    fun setExpression(exp: String) = live2DView?.setExpression(exp)

    // ───── 말풍선 제어 ─────
    fun showSpeech(
        text: String,
        tail: TailPosition = TailPosition.Left,
        align: Alignment = Alignment.BottomCenter
    ) {
        _speech.value = text
        _tailPosition.value = tail
        _alignment.value = align
    }

    // ───── 모션 리스트 갱신 ─────
    fun refreshMotions() {
        _motions.value = live2DView?.getAvailableMotions() ?: emptyList()
    }
}

@HiltViewModel
class Live2DViewModel @Inject constructor() : ViewModel() {
    // 화면 생명주기와 무관하게 Controller 유지
    val controller: Live2DController = Live2DController()

    override fun onCleared() {
        super.onCleared()
        // 필요하다면 GL 자원 해제 처리
        controller.detach()
    }
}

@Composable
fun Live2DCanvas(
    controller: Live2DController,
    modifier: Modifier = Modifier
) {
    val speech = controller.speech.collectAsState()
    val tailPosition by controller.tailPosition.collectAsState()
    val align = controller.alignment.collectAsState()

    val xOffset = when (tailPosition) {
        TailPosition.Left -> (40).dp
        TailPosition.Right -> (-40).dp
        TailPosition.Center -> 0.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        // Live2D View
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { ctx: Context ->
                Live2DView(ctx).apply {
                    controller.attachView(this)
                }
            }
        )
        // 말풍선 Overlay
        CharacterSpeechBubble(
            text = speech.value,
            tailPosition = tailPosition,
            modifier = Modifier.align(align.value).offset(x = xOffset, y = (-16).dp)

        )
    }
}

@Composable
fun Live2DTestScreen(
    viewModel: Live2DViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val controller = viewModel.controller
    val motions by controller.motions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Live2DCanvas(controller)

        Spacer(Modifier.height(16.dp))

        // 기본 제어 버튼
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { controller.nextCharacter() }) { Text("➡ 다음 캐릭터") }
            Button(onClick = { controller.changeCharacter(0) }) { Text("🔄 캐릭터 0번") }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { controller.playMotion("TapBody", 0) }) {
                Text("▶ TapBody 모션")
            }
            Button(onClick = { controller.setExpression("f00") }) {
                Text("😃 표정 변경 (f00)")
            }
        }

        Spacer(Modifier.height(16.dp))

        // 말풍선 테스트 버튼
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                controller.showSpeech("안녕하세요! 🌟", TailPosition.Left, Alignment.TopCenter)
            }) { Text("💬 좌측") }

            Button(onClick = {
                controller.showSpeech("리겔은 푸른 별이에요 ✨", TailPosition.Center, Alignment.TopCenter)
            }) { Text("💬 중앙") }

            Button(onClick = {
                controller.showSpeech("베텔게우스는 붉은 초거성입니다 🔥", TailPosition.Right, Alignment.TopCenter)
            }) { Text("💬 우측") }
        }

        Spacer(Modifier.height(16.dp))

        // 모션 목록 + 새로고침
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("📂 모션 목록")
            Button(onClick = { controller.refreshMotions() }) {
                Text("🔄 새로고침")
            }
        }

        Spacer(Modifier.height(8.dp))

        // 모션 리스트 표시
        Column {
            motions.forEach { motion ->
                val parts = motion.split(":")
                val group = parts.getOrNull(0) ?: return@forEach
                val index = parts.getOrNull(1)?.toIntOrNull() ?: 0

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { controller.playMotion(group, index) }
                ) {
                    Text("▶ $motion 실행")
                }
            }
        }
    }
}
