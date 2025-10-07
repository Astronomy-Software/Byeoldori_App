package com.example.byeoldori.ui.components.observatory

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.renderscript.RenderScript
import android.util.Log
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
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker

@Composable
fun CurrentLocationButton(
    context: Context,
    mapView: MapView,
    fusedLocationClient: FusedLocationProviderClient,
    onLocated: (Double, Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            mapView.getMapAsync {
                moveToCurrentLocation(context, fusedLocationClient, mapView, onLocated)
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


private const val TAG_LOC = "CurrentLocation"

private fun hasLocationPermission(ctx: Context): Boolean {
    val fine = ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    Log.d(TAG_LOC, "hasPermission fine=$fine coarse=$coarse")
    return fine || coarse
}

@SuppressLint("MissingPermission")
fun moveToCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    mapView: MapView,
    onLocated: (Double, Double) -> Unit = { _, _ -> }
) {
    if (!hasLocationPermission(context)) {
        Log.w(TAG_LOC, "No location permission. Abort.")
        return
    }

    val cts = CancellationTokenSource()
    Log.d(TAG_LOC, "getCurrentLocation() request...")
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        cts.token
    ).addOnSuccessListener { loc ->
        if (loc != null) {
            Log.d(TAG_LOC, "getCurrentLocation OK lat=${loc.latitude} lon=${loc.longitude}")
            onLocated(loc.latitude, loc.longitude)   // 좌표 전달
            val currentLatLng = LatLng(loc.latitude, loc.longitude)
            mapView.getMapAsync { naverMap ->
                naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng))
                Marker().apply {
                    position = currentLatLng
                    map = naverMap
                }
            }
        } else {
            Log.w(TAG_LOC, "getCurrentLocation returned NULL. Fallback to lastLocation")
            fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                if (last != null) {
                    Log.d(TAG_LOC, "lastLocation OK lat=${last.latitude} lon=${last.longitude}")
                    onLocated(last.latitude, last.longitude)
                    val currentLatLng = LatLng(last.latitude, last.longitude)
                    mapView.getMapAsync { naverMap ->
                        naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng))
                        Marker().apply {
                            position = currentLatLng
                            map = naverMap
                        }
                    }
                } else {
                    Log.e(TAG_LOC, "lastLocation is also NULL. Can't determine location.")
                }
            }
        }
    }.addOnFailureListener { e ->
        Log.e(TAG_LOC, "getCurrentLocation failure: ${e.message}", e)
    }
}
