package com.example.byeoldori.character

import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.live2d.live2dview.Live2DView

@Composable
fun Live2DScreen(
    vm: Live2DControllerViewModel = hiltViewModel()
) {
    val controller = vm.controller
    val context = LocalContext.current

    // ✅ Live2DView를 remember로 고정 (재구성 시에도 같은 인스턴스 유지)
    val live2DView = remember {
        Log.d("Live2DScreen", "✅ Live2DView 최초 생성됨")
        Live2DView(context).apply {
            controller.attachView(this)
            visibility = View.VISIBLE
        }
    }

    // 상태 구독
    val speech by controller.speech.collectAsState()
    val tail by controller.tailPosition.collectAsState()
    val align by controller.alignment.collectAsState()
    val modifier by controller.viewModifier.collectAsState()

    val xOffset = when (tail) {
        TailPosition.Left -> 40.dp
        TailPosition.Right -> (-40).dp
        TailPosition.Center -> 0.dp
    }

    // 말풍선 로그
    Log.d("Live2DScreen", "현재 말풍선 텍스트: $speech")

    // Box: Live2DView + 말풍선
    Box(modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { live2DView },
            onRelease = {
                Log.d("Live2DScreen", "❌ Live2DView 해제됨")
                controller.detachView()
            }
        )

        CharacterSpeechBubble(
            text = speech,
            tailPosition = tail,
            modifier = Modifier
                .align(align)
                .offset(x = xOffset, y = 0.dp)
        )
    }
}
