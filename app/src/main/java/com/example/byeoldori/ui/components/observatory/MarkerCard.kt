package com.example.byeoldori.ui.components.observatory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.ui.theme.Blue500
import com.example.byeoldori.ui.theme.Blue800
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.ui.theme.Purple800
import com.example.byeoldori.ui.theme.TextHighlight

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

