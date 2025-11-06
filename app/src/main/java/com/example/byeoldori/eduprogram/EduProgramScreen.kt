//package com.example.byeoldori.eduprogram
//
//import android.app.Activity
//import android.content.pm.ActivityInfo
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.core.view.WindowCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.core.view.WindowInsetsControllerCompat
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.byeoldori.character.Emotion
//import com.example.byeoldori.character.Live2DControllerViewModel
//import com.example.byeoldori.skymap.SkyMode
//import com.example.byeoldori.skymap.StellariumController
//import com.example.byeoldori.skymap.StellariumScreen
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//@Composable
//fun EduProgramScreen() {
//    val activity = LocalContext.current as Activity
//    val vm: EduViewModel = hiltViewModel()
//
//    val isLoading by vm.isLoading.collectAsState()
//    val isReady by vm.isReady.collectAsState()
//    val isStarted by vm.isStarted.collectAsState()
//    val isEnded by vm.isEnded.collectAsState()
//
//    val log by vm.log.collectAsState()
//    val timer by vm.timer.collectAsState()
//    val programTitle by vm.programTitle.collectAsState()
//    val sectionTitle by vm.title.collectAsState()
//    val sectionIndex by vm.sectionIndex.collectAsState()
//    val totalSections by vm.totalSections.collectAsState()
//    val autoPlay by vm.autoPlay.collectAsState()
//
//    // ✅ 전체화면 및 시스템바 제어
//    DisposableEffect(Unit) {
//        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//
//        val window = activity.window
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        val controller = WindowInsetsControllerCompat(window, window.decorView)
//        controller.hide(WindowInsetsCompat.Type.systemBars())
//        controller.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//
//        onDispose {
//            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//            controller.show(WindowInsetsCompat.Type.systemBars())
//            vm.stop()
//            StellariumController.clearBinding()
//        }
//    }
//
//    // ✅ 화면 진입 즉시 로딩 시작
//    LaunchedEffect(Unit) { vm.preloadScenario() }
//
//    Box(Modifier.fillMaxSize()) {
//
//        // ✅ 배경: Stellarium + Live2D
//        StellariumScreen(SkyMode.EDUCATION)
//        com.example.byeoldori.character.Live2DScreen()
//
//        // ✅ EduOverlayUI는 항상 표시
//        EduOverlayUI(
//            programTitle = programTitle,
//            sectionTitle = sectionTitle,
//            log = log,
//            timer = timer,
//            currentSection = sectionIndex,
//            totalSections = totalSections,
//            autoPlay = autoPlay,
//            enabled = !isLoading,              // 로딩 중엔 비활성화
//            onNextClick = { vm.next() },
//            onPrevClick = { vm.prev() },
//            onAutoClick = { vm.toggleAuto() },
//            onCloseClick = { vm.closeProgram() }
//        )
//
//        // ✅ 중앙 로딩 안내
//        if (isLoading) {
//            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    CircularProgressIndicator()
//                    Spacer(Modifier.height(8.dp))
//                    Text("교육 프로그램 준비 중이에요!", style = MaterialTheme.typography.titleLarge)
//                }
//            }
//        }
//
//        // ✅ 준비 완료 & 아직 시작 전 → Start 버튼
//        if (isReady && !isStarted && !isLoading) {
//            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Button(onClick = { vm.start() }) { Text("Start") }
//            }
//        }
//
//        // ✅ 교육 종료 시 엔딩 멘트
//        if (isEnded) {
//            LaunchedEffect(isEnded) {
//                Live2DControllerViewModel.chat(
//                    "모든 교육이 끝났어! 함께해줘서 고마워 ✨",
//                    Emotion.Happy
//                )
//                delay(4000)
//                Live2DControllerViewModel.playExitMotion()
//            }
//        }
//    }
//}
