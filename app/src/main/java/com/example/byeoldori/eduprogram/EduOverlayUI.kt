package com.example.byeoldori.eduprogram

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.eduprogram.EduState
import com.example.byeoldori.ui.theme.TextHighlight

// ===============================================================
// ✅ EduOverlayUI (최대 압축 + VM 내부에서 가져옴)
// ===============================================================
@Composable
fun EduOverlayUI() {
    val vm: EduViewModel = hiltViewModel()

    val programTitle by vm.programTitle.collectAsState()
    val sectionTitle by vm.title.collectAsState()
    val log by vm.log.collectAsState()
    val timer by vm.timer.collectAsState()
    val stepDuration by vm.duration.collectAsState()
    val currentSection by vm.sectionIndex.collectAsState()
    val totalSections by vm.totalSections.collectAsState()
    val autoPlay by vm.autoPlay.collectAsState()
    val state by vm.state.collectAsState()

    Box(Modifier.fillMaxSize()) {

        if (state is EduState.Ready) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { vm.start() }) { Text("교육 시작!") }
            }
        }

        Text(
            "$programTitle - $sectionTitle",
            style = MaterialTheme.typography.titleLarge,
            color = TextHighlight,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { vm.toggleAuto() },
            ) {
                Text(if (autoPlay) "자동 ON" else "자동 OFF")
            }

            Button(onClick = { vm.closeProgram() }) {
                Text("종료")
            }
        }
        // TODO : 테스트 끝나면 log는 삭제
        Text(
            text =
            if (totalSections > 0 && currentSection >= 0)
                "SECTION ${currentSection + 1}/$totalSections · $log"
            else log,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (autoPlay && stepDuration > 0 && timer in 1..stepDuration) {
                val progress = (1f - timer.toFloat() / stepDuration).coerceIn(0f, 1f)
                CircularProgressIndicator(progress = { progress })
            }
            Button(onClick = { vm.prev() }) { Text("이전") }
            Button(onClick = { vm.next() }) { Text("다음") }
        }
    }
}