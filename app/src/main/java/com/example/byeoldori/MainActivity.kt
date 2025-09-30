package com.example.byeoldori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.example.byeoldori.ui.AppEntry
import com.example.byeoldori.ui.theme.AppTheme
import com.live2d.sdk.cubism.framework.CubismFramework
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 네비게이션 바 색상 바꾸기
        window.navigationBarColor = android.graphics.Color.parseColor("#48287B") // 이 부분은 Color 를 사용할 수 없음. purple 800 사용함.
        // 네비게이션 아이콘 색상 밝기 설정 (밝은 배경일 때 true)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightNavigationBars = false // false = 흰색 아이콘, true = 검정 아이콘
        // (선택) 앱 전역 몰입형으로 쓰고 싶다면 주석 해제
        /*
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        */
        // Live2D 초기화

        // 초기화 코드
        // Live2D Cubism Framework 초기화 (Java SDK 버전)
        if (!CubismFramework.isStarted()) {
            CubismFramework.startUp(null)   // Java SDK는 Option 없이 null 가능
            CubismFramework.initialize()
        }

        setContent {
            AppTheme {
                AppEntry()
            }
        }
    }
}

