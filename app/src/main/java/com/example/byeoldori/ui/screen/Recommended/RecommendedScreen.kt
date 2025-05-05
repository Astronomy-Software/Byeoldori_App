package com.example.byeoldori.ui.screen.Recommended

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.viewmodel.AppScreen
import com.example.byeoldori.viewmodel.NavigationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedScreen() {
    val navViewModel: NavigationViewModel = viewModel()
    Scaffold(
        topBar = { TopAppBar(title = { Text("🔭 추천 관측 대상") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {navViewModel.navigateTo(AppScreen.SkyMap)}) {
                Text("← 홈으로 돌아가기")
            }
        }
    }
}
