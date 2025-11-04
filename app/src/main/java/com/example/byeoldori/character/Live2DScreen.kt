package com.example.byeoldori.character

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.live2d.live2dview.LAppDelegate

// Context에서 Activity를 찾기 위한 헬퍼 함수
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Jetpack Compose 환경에서 Live2D 모델을 렌더링하는 Composable View입니다.
 */
@Composable
fun Live2DScreen(
    vm: Live2DControllerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val activity = context.findActivity()
        ?: throw IllegalStateException("Live2DComposeView must be used within an Activity context.")

    val controller = vm.controller
    val live2DView = remember {
        Live2DGLSurfaceView(context).apply {
            controller.attachView(this)
        }
    }

    // 상태 구독
    val speech by controller.speech.collectAsState()
    val tail by controller.tailPosition.collectAsState()
    val align by controller.alignment.collectAsState()
    val modifier by controller.viewModifier.collectAsState()
    val bubbleYOffset by controller.bubbleYOffset.collectAsState() // ViewModel에서 세로 위치 관리
    val isVisible by controller.isVisible.collectAsState()

    // 꼬리 위치에 따른 가로 오프셋 (DP)
    val xOffset = when (tail) {
        TailPosition.Left -> 40.dp
        TailPosition.Right -> (-40).dp
        TailPosition.Center -> 0.dp
    }

    // DP 값을 픽셀 정수(IntOffset)로 변환하여 Popup에 전달
    val pixelOffset = remember(xOffset, bubbleYOffset, density) {
        with(density) {
            IntOffset(
                x = xOffset.roundToPx(),
                y = bubbleYOffset.roundToPx()
            )
        }
    }

    Box(modifier) {
        // Live2DGLSurfaceView를 Compose 트리에 통합
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                LAppDelegate.getInstance().onStart(activity)
                // Live2D Surface 투명도 확보 및 Z-Order 최상단 설정
                live2DView.setZOrderOnTop(true)
                live2DView
            }
        )

        // Compose Lifecycle 관리
        DisposableEffect(Unit) {
            live2DView.onResume()
            onDispose {
                live2DView.onPause()
                LAppDelegate.getInstance().onStop()
                LAppDelegate.getInstance().onDestroy()
                LAppDelegate.releaseInstance()
            }
        }

        // 팝업 생성 로직은 CharacterSpeechBubble로 이동
        if (isVisible) {
            CharacterSpeechBubble(
                text = speech,
                tailPosition = tail,
                alignment = align,      // Popup의 기준 정렬
                pixelOffset = pixelOffset, // Popup의 픽셀 오프셋 (머리 중앙 위치)
                modifier = Modifier
            )
        }
    }
}