package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservatoryScreen(
    onBackToHome: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        NavermapScreen(onBack = onBackToHome)
    }
}