package com.example.byeoldori.ui.components.observatory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.byeoldori.ui.theme.TextHighlight
import com.naver.maps.geometry.LatLng

@Composable
fun LocationInfoBox( //com
    latLng: LatLng,
    address: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .wrapContentWidth(Alignment.Start)
            .background(TextHighlight)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = address, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "위도: ${latLng.latitude}, 경도: ${latLng.longitude}",
                color = Color.Gray, // 여기색상도 바꿔주어야함
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}