//package com.example.byeoldori.eduprogram
//
//import EduOverlayUI
//import android.app.Activity
//import android.content.pm.ActivityInfo
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.core.view.WindowCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.core.view.WindowInsetsControllerCompat
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.byeoldori.character.Live2DController
//import com.example.byeoldori.skymap.ObjectDetailViewModel
//import com.example.byeoldori.skymap.SkyMode
//import com.example.byeoldori.skymap.StellariumController
//import com.example.byeoldori.skymap.StellariumScreen
//
//@Composable
//fun EduProgramScreen() {
//    val context = LocalContext.current
//    val activity = context as Activity
//    val window = activity.window
//
//    // 🌟 ViewModels
//    val skyViewModel: ObjectDetailViewModel = hiltViewModel()
//    val eduViewModel: EduViewModel = hiltViewModel()
//
//    // 🌟 상태
//    val isBound by StellariumController.isBound.collectAsState()
//    val log by eduViewModel.log.collectAsState()
//
//    // Live2D (지금은 임시로 Compose 내부 관리)
//    val live2DController = remember { Live2DController() }
//
//    // ✅ 시스템 UI 숨김 + 가로모드
//    DisposableEffect(Unit) {
//        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        val controller = WindowInsetsControllerCompat(window, window.decorView)
//        controller.hide(WindowInsetsCompat.Type.systemBars())
//        controller.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//
//        onDispose {
//            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//            controller.show(WindowInsetsCompat.Type.systemBars())
//        }
//    }
//
//    // 🌌 Stellarium + 오버레이 표시
//    Box(modifier = Modifier.fillMaxSize()) {
//        StellariumScreen(SkyMode.EDUCATION)
//        EduOverlayUI(log = log)
//    }
//
//    // ✅ StellariumController 바인딩 완료 시 EduEngine 초기화
//    LaunchedEffect(isBound) {
//        if (isBound) {
//            eduViewModel.loadAndInitialize(context)
//            println("✅ EduEngine 초기화 및 시나리오 실행 시작")
//        }
//    }
//
//    // 🧹 화면 종료 시 정리
//    DisposableEffect(Unit) {
//        onDispose {
//            StellariumController.clearBinding()
//            eduViewModel.stopProgram()
//            println("🧹 EduProgramScreen 종료 — 리소스 해제 완료")
//        }
//    }
//}
