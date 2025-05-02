package com.example.byeoldori.ui.screen.Recommended

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedScreen(onNavigateTo: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("🔭 추천 관측 대상") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onNavigateTo) {
                Text("← 홈으로 돌아가기")
            }
        }
    }
}
