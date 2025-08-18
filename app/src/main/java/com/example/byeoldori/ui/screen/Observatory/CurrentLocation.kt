package com.example.byeoldori.ui.screen.Observatory

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.ui.theme.TextHighlight
import com.google.android.gms.location.FusedLocationProviderClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker

@Composable
fun CurrentLocationButton(
    context: Context,
    mapView: MapView,
    fusedLocationClient: FusedLocationProviderClient,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            mapView.getMapAsync {
                moveToCurrentLocation(context, fusedLocationClient, mapView)
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple500,
            contentColor = TextHighlight
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(50.dp)
    ) {
        Text("현재 위치", fontSize = 12.sp)
    }
}


fun moveToCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    mapView: MapView
) {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mapView.getMapAsync { naverMap ->
                    naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng))
                    Marker().apply {
                        position = currentLatLng
                        map = naverMap
                        this.iconTintColor = android.graphics.Color.BLACK // 검정색에대한 색상정보가 없어 이부분은 Color에 추가하고 바꿔야함 회의내용
                    }
                }
            }
        }
    }
}