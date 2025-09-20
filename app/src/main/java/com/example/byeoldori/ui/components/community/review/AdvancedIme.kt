package com.example.byeoldori.ui.components.community.review

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity

//ImePadding 이중 패딩 문제 해결
fun Modifier.advancedImePadding(): Modifier = composed {
    var consumePaddingPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    onGloballyPositioned { coords ->
        val root = coords.findRootCoordinates()
        val bottom = coords.positionInWindow().y + coords.size.height
        consumePaddingPx = (root.size.height - bottom).toInt().coerceAtLeast(0)
    }
        // 먼저 "중복되는 부분만" 인셋으로 소비해 하위로 전파 차단
        .consumeWindowInsets(
            PaddingValues(bottom = with(density) { consumePaddingPx.toDp() })
        )
        .imePadding()
}