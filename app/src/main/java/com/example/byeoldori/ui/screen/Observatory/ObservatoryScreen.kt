package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservatoryScreen(
    onBackToHome: () -> Unit,
    onNavigateToNaverMap: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("📍 관측지") }) }
    ) { padding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            //contentAlignment = Alignment.Center
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onBackToHome) {
                Text("← 홈으로 돌아가기")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToNaverMap) {
                Text("네이버 지도 보기")
            }
        }
    }
}
