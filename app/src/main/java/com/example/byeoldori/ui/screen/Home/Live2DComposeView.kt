package com.example.byeoldori.ui.screen.Home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.live2d.live2dview.Live2DView

@Composable
fun Live2DComposeView(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()   // 가로는 꽉 채우고
            .height(300.dp),  // 세로는 원하는 높이 지정,
        factory = { context: Context ->
            Log.d("Live2D", "Live2DSampleView init in Compose")
            Live2DView(context)
        },
        update = { view ->
            // 필요하면 업데이트 로직 추가
        }
    )
}
