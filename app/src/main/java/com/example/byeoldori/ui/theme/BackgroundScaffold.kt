package com.example.byeoldori.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun BackgroundScaffold(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 0) 배경
        Background(
            modifier = Modifier.fillMaxSize(),
            isAnimating = isAnimating
        ) { /* 배경 자체에는 콘텐츠 없음 */ }

        // 1) Scaffold (투명)
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0), // Scaffold의 기본 인셋 제거
            topBar = { topBar?.invoke() },
            bottomBar = { bottomBar?.invoke() },
            floatingActionButton = { floatingActionButton?.invoke() },
            snackbarHost = { snackbarHost?.invoke() }
        ) { innerPadding ->
            // 자식 화면은 innerPadding만 적용받으면 됨
            content(innerPadding)
        }
    }
}
