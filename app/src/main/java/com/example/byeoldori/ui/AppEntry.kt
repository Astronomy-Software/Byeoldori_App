package com.example.byeoldori.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.character.Live2DScreen
import com.example.byeoldori.ui.screen.SplashScreen
import com.example.byeoldori.viewmodel.AuthViewModel

// 앱의 진입점 역할, Login 여부에 따라 Auth 혹은 Main 으로 전환
@Composable
fun AppEntry() {
    var showSplash  by remember { mutableStateOf(true) }

    if (showSplash) {
        // 기본적인 초기화내용은 splash 화면내부에서 진행
        SplashScreen(onSplashFinished = {
            showSplash = false
        })
        return
    }

    val authVm: AuthViewModel = hiltViewModel() // ✅ Hilt ViewModelFactory 사용
    val signedIn by authVm.isSignedIn.collectAsState()

    Box {
        if (signedIn) {
            MainRoot()
        } else {
            AuthRoot()
        }
        Live2DScreen()
    }
}
