package com.example.byeoldori.eduprogram

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.byeoldori.skymap.SkyMode
import com.example.byeoldori.skymap.StellariumScreen

@Composable
fun EduProgramScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val window = activity?.window

    // ✅ 가로모드 + 시스템바 숨김
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            val controller = WindowInsetsControllerCompat(it, it.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window?.let {
                val controller = WindowInsetsControllerCompat(it, it.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // ✅ Stellarium 화면 + 교육용 오버레이
    Box(modifier = Modifier.fillMaxSize()) {
        StellariumScreen(SkyMode.EDUCATION) // TODO : 몰입형 UI로 변경하기 함수 추가

        // 🎓 교육 오버레이 (버튼, 상태 텍스트 등)
        EduOverlayUI()
    }
}
