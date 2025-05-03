package com.example.byeoldori.ui.screen.Observatory

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
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
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier = modifier
    ) {
        Text("현재 위치")
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
                        this.iconTintColor = android.graphics.Color.BLACK
                    }
                }
            }
        }
    }
}