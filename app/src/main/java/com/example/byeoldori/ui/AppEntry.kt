package com.example.byeoldori.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.screen.SplashScreen
import com.example.byeoldori.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

// 앱의 진입점 역할, Login 여부에 따라 Auth 혹은 Main 으로 전환
@Composable
fun AppEntry() {
    // 초기화(네트워크/로컬 복원 등)
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // TODO 실제 초기화 로직
        delay(1000)
        isInitialized = true
    }

    if (!isInitialized) {
        // TODO Splash Screen 디자이너와 같이 생각해보기
        SplashScreen(onSplashFinished = {})   // 컴포저블 스플래시
        return
    }

    // 로그인 분기 → MainRoot or AuthRoot
    val authVm: AuthViewModel = hiltViewModel() // ✅ Hilt ViewModelFactory 사용
    val signedIn = true

    if (signedIn) {
        MainRoot(onSignOut = { authVm.signOut() })
    } else {
        AuthRoot(onSignedIn = {  })
    }
}
