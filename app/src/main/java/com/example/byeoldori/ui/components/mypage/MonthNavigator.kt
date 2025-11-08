package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import java.time.YearMonth

@Composable
fun MonthNavigator(
    ym: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    textColor: Color = Color.White
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "이전 달", tint = textColor)
        }
        Text(
            text = "${ym.year}년 ${ym.monthValue}월",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.ArrowForwardIos, contentDescription = "다음 달", tint = textColor)
        }
    }
}