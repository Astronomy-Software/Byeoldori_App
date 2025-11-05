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
import com.live2d.live2dview.LAppDelegate

// Context에서 Activity를 찾는 헬퍼
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Jetpack Compose에서 Live2D 캐릭터를 표시하는 Composable.
 * 이제 전역 싱글톤 기반 ViewModel을 직접 참조함.
 */
@Composable
fun Live2DScreen() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val activity = context.findActivity()
        ?: throw IllegalStateException("Live2DScreen은 Activity 컨텍스트 내에서만 사용 가능합니다.")

    // 전역 Live2D 컨트롤러 접근
    val controller = Live2DControllerViewModel.controller

    // Live2D View 생성 및 attach
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
    val bubbleYOffset by controller.bubbleYOffset.collectAsState()
    val isVisible by controller.isVisible.collectAsState()

    // 꼬리 위치에 따른 가로 오프셋 계산
    val xOffset = when (tail) {
        TailPosition.Left -> 40.dp
        TailPosition.Right -> (-40).dp
        TailPosition.Center -> 0.dp
    }

    // DP → 픽셀 변환
    val pixelOffset = remember(xOffset, bubbleYOffset, density) {
        with(density) {
            IntOffset(
                x = xOffset.roundToPx(),
                y = bubbleYOffset.roundToPx()
            )
        }
    }

    // 본문 UI
    Box(modifier) {
        // Live2DGLSurfaceView를 Compose 트리에 삽입
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                LAppDelegate.getInstance().onStart(activity)
                live2DView.setZOrderOnTop(true)
                live2DView
            }
        )

        // 생명주기 관리
        DisposableEffect(Unit) {
            live2DView.onResume()
            onDispose {
                live2DView.onPause()
                LAppDelegate.getInstance().onStop()
                LAppDelegate.getInstance().onDestroy()
                LAppDelegate.releaseInstance()
            }
        }

        // 말풍선 표시
        if (isVisible) {
            CharacterSpeechBubble(
                text = speech,
                tailPosition = tail,
                alignment = align,
                pixelOffset = pixelOffset,
                modifier = Modifier
            )
        }
    }
}
