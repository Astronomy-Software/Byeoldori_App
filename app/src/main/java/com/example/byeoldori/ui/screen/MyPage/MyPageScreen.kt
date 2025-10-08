package com.example.byeoldori.ui.screen.MyPage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.byeoldori.character.Live2DTestUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("👤 마이페이지") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Live2DTestUI()
        }
    }
}
