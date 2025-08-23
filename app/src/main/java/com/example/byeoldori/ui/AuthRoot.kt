package com.example.byeoldori.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.byeoldori.ui.screen.Login.LoginScreen
import com.example.byeoldori.ui.theme.Background

@Composable
fun AuthRoot(onSignedIn: () -> Unit) {
    // TODO UI 연결하기 및 구조 정립
    Background(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            LoginScreen(onSignIn = onSignedIn)
        }
    }
}