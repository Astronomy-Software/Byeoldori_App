package com.example.byeoldori.ui.components.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.ui.theme.*
import androidx.compose.ui.window.Dialog


@Composable
fun ScoreLabel (
    show: Boolean,
    score: Int,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    if(!show) return
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "관측지 점수를 선택해주세요~~! (1~5)",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    (1..5).forEach { n ->
                        val selected = score == n
                        Button(
                            onClick = { onSelected(n) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) Purple800 else Color.LightGray,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp) //기본 패딩 제거
                        ) {
                            Text("$n")
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("확인")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun Preview_ScoreLabel() {
    MaterialTheme {
        var score by remember { mutableStateOf(5) }
        // 항상 보이도록 show = true
        ScoreLabel(
            show = true,
            score = score,
            onSelected = { n -> score = n },
            onDismiss = {  }
        )
    }
}
