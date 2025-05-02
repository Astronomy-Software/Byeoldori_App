package com.example.byeoldori.ui

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.byeoldori.viewmodel.*
import com.example.byeoldori.ui.screen.*
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ByeoldoriApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val navViewModel: NavigationViewModel = viewModel()

    // 시스템 UI 설정
    LaunchedEffect(Unit) {
        activity?.window?.let { window ->
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    // 초기화 플로우
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1000)
        isInitialized = true
    }

    if (!isInitialized) {
        SplashScreen(onSplashFinished = {})
        return
    }

    // 💡 모든 분기는 HomeScreen에게 위임
    HomeScreen()
}
