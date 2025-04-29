package com.example.byeoldori

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.byeoldori.ui.ByeoldoriApp

class MainActivity : ComponentActivity() { //ComponentActivity는 Jetpack Compose를 사용할 수 있게 도와주는 기반 클래스
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false) //상단 바, 하단 내비게이션 바 화면 확장해서 전체화면으로 보여줌

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        setContent {
            ByeoldoriApp()

            LaunchedEffect(Unit) { //상단 상태 바와 하단 네비게이션 바를 숨기고, 풀스크린으로
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

