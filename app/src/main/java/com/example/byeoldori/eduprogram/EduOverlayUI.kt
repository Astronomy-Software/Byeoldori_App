package com.example.byeoldori.eduprogram

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.byeoldori.ui.theme.TextHighlight

@Composable
fun EduOverlayUI(
    onPrevClick: (() -> Unit)? = null,
    onNextClick: (() -> Unit)? = null,
    onStopClick: (() -> Unit)? = null,
    currentStepText: String = "별자리 위치 학습"
) {
    Box(modifier = Modifier.fillMaxSize()) {

        Text(
            text = currentStepText,
            color = TextHighlight,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 16.dp)
        )

        Button(
            onClick = { onStopClick?.invoke() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 20.dp, top = 12.dp)
                .height(36.dp)
        ) {
            Text("교육 종료", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        }

        Button(
            onClick = { onPrevClick?.invoke() },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 12.dp)
                .height(36.dp)
        ) {
            Text("이전", fontSize = MaterialTheme.typography.bodyLarge.fontSize)
        }

        Button(
            onClick = { onNextClick?.invoke() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 12.dp)
                .height(36.dp)
        ) {
            Text("다음", fontSize = MaterialTheme.typography.bodyLarge.fontSize)
        }
    }
}
