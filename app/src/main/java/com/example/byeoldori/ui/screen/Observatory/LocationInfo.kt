package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng

@Composable
fun LocationInfoBox(
    latLng: LatLng,
    address: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .wrapContentWidth(Alignment.Start)
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
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