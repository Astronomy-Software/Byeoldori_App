//package com.example.byeoldori.eduprogram
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun EduOverlayUI(
//    programTitle: String,
//    sectionTitle: String,
//    log: String,
//    timer: Long,
//    currentSection: Int,
//    totalSections: Int,
//    autoPlay: Boolean,
//    enabled: Boolean = true,
//    onNextClick: () -> Unit,
//    onPrevClick: () -> Unit,
//    onAutoClick: () -> Unit,
//    onCloseClick: () -> Unit
//) {
//    Box(Modifier.fillMaxSize()) {
//
//        // ✅ 상단 좌측 — 프로그램 제목 + 섹션 제목
//        Text(
//            text = "$programTitle - $sectionTitle",
//            style = MaterialTheme.typography.titleLarge,
//            modifier = Modifier
//                .align(Alignment.TopStart)
//                .padding(16.dp)
//        )
//
//        // ✅ 상단 우측 — 자동 넘김 + 종료
//        Row(
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(12.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            OutlinedButton(
//                onClick = onAutoClick,
//                enabled = enabled
//            ) {
//                Text(if (autoPlay) "자동 ON" else "자동 OFF")
//            }
//
//            Button(onClick = onCloseClick) {
//                Text("종료")
//            }
//        }
//
//        // ✅ 하단 중앙 — 상태 로그
//        Text(
//            text = if (totalSections > 0 && currentSection >= 0)
//                "SECTION ${currentSection + 1}/$totalSections · $log"
//            else log,
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 64.dp)
//        )
//
//        // ✅ 하단 우측 — 타이머 + 이전/다음 버튼
//        Column(
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(24.dp),
//            horizontalAlignment = Alignment.End,
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            // ✅ 자동 넘김 활성화 + duration 존재할 때만 진행 표시
//            if (autoPlay && timer > 0) {
//                CircularProgressIndicator(
//                    progress = { (timer / 10000f).coerceIn(0f, 1f) }
//                )
//            }
//
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                OutlinedButton(onClick = onPrevClick, enabled = enabled) {
//                    Text("이전")
//                }
//                Button(onClick = onNextClick, enabled = enabled) {
//                    Text("다음")
//                }
//            }
//        }
//    }
//}
