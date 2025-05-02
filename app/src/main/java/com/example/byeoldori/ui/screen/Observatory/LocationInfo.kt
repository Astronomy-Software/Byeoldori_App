package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.*
import com.naver.maps.geometry.LatLng

@Composable
fun LocationInfoBox(latLng: LatLng, address: String) {
    Box(
        modifier = Modifier
            .offset(x = (-130).dp, y = (-290).dp)
            .padding(16.dp)
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = address, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "위도: ${latLng.latitude}, 경도: ${latLng.longitude}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
