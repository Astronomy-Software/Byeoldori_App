package com.example.byeoldori.eduprogram

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.skymap.SkyMode
import com.example.byeoldori.skymap.StellariumController
import com.example.byeoldori.skymap.StellariumScreen

// ===============================================================
// ✅ EduProgramScreen
// ===============================================================
@Composable
fun EduProgramScreen() {
    val activity = LocalActivity.current ?: return
    val vm: EduViewModel = hiltViewModel()
    val feedbackVm: EduFeedbackViewModel = hiltViewModel() // ✅ 피드백 VM도 주입
    val context = LocalContext.current

    // ✅ EduViewModel의 programId를 감시하여 feedbackVm에 전달
    val programId by vm.programId.collectAsState()

    LaunchedEffect(programId) {
        programId?.let {
            feedbackVm.updatepostId(it)
            println("✅ FeedbackViewModel postId 업데이트 완료: $it")
        }
    }

    DisposableEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            controller.show(WindowInsetsCompat.Type.systemBars())
            vm.stop()
            StellariumController.clearBinding()
        }
    }

    LaunchedEffect(Unit) { vm.preloadScenario(context) }

    Box(Modifier.fillMaxSize()) {
        StellariumScreen(SkyMode.EDUCATION)
        EduOverlayUI()
    }
}