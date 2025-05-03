package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.viewmodel.AppScreen
import com.example.byeoldori.viewmodel.NavigationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservatoryScreen() {
    val navViewModel: NavigationViewModel = viewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        NavermapScreen()
        // 오른쪽 상단 돌아가기 버튼
        IconButton(
            onClick = {navViewModel.navigateTo(AppScreen.SkyMap) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top=20.dp, start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기"
            )
        }
    }
}
