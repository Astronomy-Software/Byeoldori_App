package com.example.byeoldori.ui.screen.SkyMap

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.byeoldori.viewmodel.AppScreen

@Composable
fun SkyMapScreen(
    onNavigateTo: (AppScreen) -> Unit
) {
    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).padding(32.dp)) {
            Button(onClick = { onNavigateTo(AppScreen.Observatory) }) {
                Text("🔭 관측소로")
            }
            Button(onClick = { onNavigateTo(AppScreen.MyPage) }) {
                Text("👤 마이페이지로")
            }
            Button(onClick = { onNavigateTo(AppScreen.Recommended) }) {
                Text("⭐ 추천관측대상")
            }
        }
    }
}

