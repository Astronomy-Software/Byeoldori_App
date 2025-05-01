package com.example.byeoldori.ui

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.byeoldori.ui.screen.*
import com.example.byeoldori.ui.screen.MyPage.MyPageScreen
import com.example.byeoldori.ui.screen.Observatory.NavermapScreen
import com.example.byeoldori.ui.screen.Observatory.ObservatoryScreen
import com.example.byeoldori.ui.screen.SkyMap.SkyMapScreen

@Composable
fun ByeoldoriApp() {
    val context = LocalContext.current
    val window = (context as? Activity)?.window ?: return

    // ✅ 시스템 바 숨기기
    LaunchedEffect(Unit) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    var showSplash by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(Screen.Home) } //현재 어떤 화면이 표시되어야 하는지

    when {
        showSplash -> SplashScreen(onSplashFinished = { showSplash = false })

        currentScreen == Screen.Home -> HomeScreen(
            onNavigateToSkyMap = { currentScreen = Screen.SkyMap },
            onNavigateToObservatory = { currentScreen = Screen.Observatory },
            onNavigateToMyPage = { currentScreen = Screen.MyPage },
            onNavigateToRecommended = { currentScreen = Screen.Recommended }
        )

        currentScreen == Screen.SkyMap -> SkyMapScreen(onBackToHome = { currentScreen = Screen.Home })
        currentScreen == Screen.Observatory -> ObservatoryScreen(
            onBackToHome = { currentScreen = Screen.Home },
            )
        currentScreen == Screen.MyPage -> MyPageScreen(onBackToHome = { currentScreen = Screen.Home })
        currentScreen == Screen.Recommended -> RecommendedScreen(onBackToHome = { currentScreen = Screen.Home })
    }
}

