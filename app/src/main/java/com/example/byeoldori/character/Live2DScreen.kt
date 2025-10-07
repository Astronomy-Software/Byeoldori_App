package com.example.byeoldori.character

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.live2d.live2dview.LAppDelegate

/**
 * Context에서 Activity를 찾기 위한 헬퍼 함수입니다.
 * LAppDelegate.onStart(Activity) 호출에 필요합니다.
 */
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Jetpack Compose 환경에서 Live2D 모델을 렌더링하는 Composable View입니다.
 * AndroidView와 Compose의 라이프사이클 관리를 사용하여 Live2D 뷰를 통합합니다.
 */
@Composable
fun Live2DScreen(
    vm: Live2DControllerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
    val bubbleYOffset by controller.bubbleYOffset.collectAsState()
    val isVisible by controller.isVisible.collectAsState() // ✅ 추가된 visibility 상태

    val xOffset = when (tail) {
        TailPosition.Left -> 40.dp
        TailPosition.Right -> (-40).dp
        TailPosition.Center -> 0.dp
    }

    Box(modifier) {
        // AndroidView를 사용하여 Live2DGLSurfaceView를 Compose 트리에 통합합니다.
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                LAppDelegate.getInstance().onStart(activity)
                live2DView.setZOrderOnTop(true)
                live2DView
            }
        )

        // Compose Lifecycle 관리 (onResume, onPause, onDestroy)
        DisposableEffect(Unit) {
            // Composable이 화면에 표시될 때 (onStart/onResume 상당)
            live2DView.onResume()

            onDispose {
                // Composable이 화면에서 제거될 때 (onPause/onDestroy 상당)
                live2DView.onPause() // GLSurfaceView의 렌더링 스레드를 일시 중지

                // LAppDelegate의 종료 로직 실행
                LAppDelegate.getInstance().onStop()
                LAppDelegate.getInstance().onDestroy()
                LAppDelegate.releaseInstance()
            }
        }
        // ✅ 캐릭터가 보일 때만 말풍선 출력
        if (isVisible) {
            CharacterSpeechBubble(
                text = speech,
                tailPosition = tail,
                modifier = Modifier
                    .align(align)
                    .offset(x = xOffset, y = bubbleYOffset)
            )
        }
    }
}