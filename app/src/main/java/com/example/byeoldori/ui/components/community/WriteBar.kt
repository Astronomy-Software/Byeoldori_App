package com.example.byeoldori.ui.components.community

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R

@Composable
fun WriteBar (
    onSubmit: () -> Unit,
    onTempSave: () -> Unit,
    onCancel: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
            //.offset(y = (10).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCancel,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_before),
                contentDescription = "뒤로가기",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onMore,
            modifier = Modifier.offset(x=30.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more),
                contentDescription = "더보기",
                tint = Color.White
            )
        }
        TextButton(
            onClick = onTempSave,
            modifier = Modifier.offset(x=20.dp)
        ) {
            Text("임시 저장", color = Color.White)
        }
        TextButton(onClick = onSubmit) { Text("등록", color = Color.White) }
    }
}

@Preview(
    name = "WriteBar",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_WriteBar() {
    MaterialTheme {
        WriteBar(onSubmit = {}, onTempSave = {}, onCancel = {}, onMore = {})
    }
}