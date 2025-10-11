package com.example.byeoldori.ui.theme

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat

@Composable
fun SystemBars() {
    val window = (LocalActivity.current as Activity).window

    SideEffect {
        // 1️⃣ 시스템 바 영역까지 콘텐츠 확장
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2️⃣ 시스템 바 색상을 완전 투명하게 설정
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // 3️⃣ 아이콘 색상 (밝기) 제어
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = false      // false = 흰색 아이콘
        controller.isAppearanceLightNavigationBars = false   // false = 흰색 아이콘

        // 4️⃣ Android 10(Q) 이상 자동 대비 보정 끄기 (투명도 깨짐 방지)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }
}
