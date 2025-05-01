package com.example.byeoldori.ui.screen.Observatory

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker


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
