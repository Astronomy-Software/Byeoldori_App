package com.example.byeoldori.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSkyMap: () -> Unit,
    onNavigateToObservatory: () -> Unit,
    onNavigateToMyPage: () -> Unit,
    onNavigateToRecommended: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding), // ✅ 여기에서 사용해야 경고 사라짐
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = onNavigateToSkyMap) {
                    Text("🌌 별지도")
                }
                Button(onClick = onNavigateToObservatory) {
                    Text("📍 관측지")
                }
                Button(onClick = onNavigateToMyPage) {
                    Text("👤 마이페이지")
                }
                Button(onClick = onNavigateToRecommended) {
                    Text("🔭 추천 관측 대상")
                }
            }
        }
    }
}
