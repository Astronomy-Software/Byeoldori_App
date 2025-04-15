package com.example.byeoldori.ui.screen.SkyMap

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyMapScreen(onBackToHome: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("🌌 별지도") }) }
    ) { padding ->
        Button(
            onClick = onBackToHome,
            modifier = Modifier.padding(padding).padding(32.dp)  // ✅ 두 번 padding 가능
        ) {
            Text("← 홈으로 돌아가기")
        }
    }
}
