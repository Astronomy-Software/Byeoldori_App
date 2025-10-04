package com.example.byeoldori.character

import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.live2d.live2dview.Live2DView

@Composable
fun Live2DScreen(
    vm: Live2DControllerViewModel = hiltViewModel()
) {
    val controller = vm.controller
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // ✅ Live2DView를 화면 진입 시 생성
    val live2DView = Live2DView(context).apply {
        controller.attachView(this)
        visibility = View.VISIBLE
    }

    // 상태 구독
    val speech by controller.speech.collectAsState()
    val tail by controller.tailPosition.collectAsState()
    val align by controller.alignment.collectAsState()
    val modifier by controller.viewModifier.collectAsState()
    val bubbleYOffset by controller.bubbleYOffset.collectAsState()

    val xOffset = when (tail) {
        TailPosition.Left -> 40.dp
        TailPosition.Right -> (-40).dp
        TailPosition.Center -> 0.dp
    }

    Box(modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { live2DView },
            onRelease = {
                Log.d("Live2DScreen", "❌ Live2DView 해제됨")
                controller.detachView()
            }
        )

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        Log.d("Live2DScreen", "▶ onResume → Live2DView Resume")
                        live2DView.onResume()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        Log.d("Live2DScreen", "⏸ onPause → Live2DView Pause")
                        live2DView.onPause()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        Log.d("Live2DScreen", "💀 onDestroy → release Live2D")
                        controller.detachView()
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }



        CharacterSpeechBubble(
            text = speech,
            tailPosition = tail,
            modifier = Modifier
                .align(align)
                .offset(x = xOffset, y = bubbleYOffset)
        )
    }
}
