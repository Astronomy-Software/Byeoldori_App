package com.example.byeoldori.ui.components.observatory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.ui.theme.*

@Composable
fun MarkerCardWithGradient() {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(brush = Brush.verticalGradient(colors = listOf(Blue800, Purple800)))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Purple500, // 일반 관측지 마커색
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("일반 관측지", color = TextHighlight, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Blue500, // 인기 관측지 마커색
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("인기 관측지", color = TextHighlight, fontSize = 10.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_MarkerCardWithGradient() {
    Surface(color = Color.Black) {
        MarkerCardWithGradient()
    }
}

