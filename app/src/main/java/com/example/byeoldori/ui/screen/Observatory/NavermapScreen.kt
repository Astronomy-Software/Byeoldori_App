package com.example.byeoldori.ui.screen.Observatory

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.viewmodel.Observatory.MarkerInfo
import com.example.byeoldori.viewmodel.NavigationViewModel
import com.example.byeoldori.viewmodel.Observatory.NaverMapWithSearchUI
import com.google.accompanist.permissions.*
import com.naver.maps.geometry.LatLng

private const val TAG_SCREEN = "NavermapScreen"

@Composable
fun NavermapScreen(
    searchQuery: String,
    onSearch: (String) -> Unit,
    showOverlay: Boolean,
    searchTrigger: Int,
    onLatLngUpdated: (LatLng)->Unit,     // 추가
    onAddressUpdated: (String)->Unit,
    onMarkerClick: (MarkerInfo) -> Unit,  // 추가
    modifier: Modifier = Modifier,
    onCurrentLocated: (Double, Double) -> Unit
) {


    RequestLocationPermission {
        Box(modifier = Modifier.fillMaxSize()) {
            // 지도 및 검색 UI
            NaverMapWithSearchUI(
                modifier = Modifier.fillMaxSize(),
                searchQuery  = searchQuery,
                onSearchRequested = onSearch,
                searchTrigger       = searchTrigger,
                onLatLngUpdated     = onLatLngUpdated,
                onAddressUpdated    = onAddressUpdated,
                showOverlay         = showOverlay,
                onMarkerClick       = onMarkerClick,
                onCurrentLocated    = { lat, lon ->
                    Log.d(TAG_SCREEN, "onCurrentLocated from UI: lat=$lat lon=$lon")
                    onCurrentLocated(lat, lon) // 부모(ObservatoryScreen)로 그대로 전달
                }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(onPermissionGranted: @Composable () -> Unit) {
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!permissions.permissions.any { it.status.isGranted }) {
            permissions.launchMultiplePermissionRequest()
        }
    }

    if (permissions.permissions.any { it.status.isGranted }) {
        val grantedAny = permissions.permissions.any { it.status.isGranted }
        if (grantedAny) {
            val fine =
                permissions.permissions.firstOrNull { it.permission == Manifest.permission.ACCESS_FINE_LOCATION }?.status?.isGranted == true
            val coarse =
                permissions.permissions.firstOrNull { it.permission == Manifest.permission.ACCESS_COARSE_LOCATION }?.status?.isGranted == true
            Log.d(TAG_SCREEN, "Permissions granted -> fine=$fine, coarse=$coarse")
            onPermissionGranted()
        } else if (permissions.shouldShowRationale) {
            Log.w(TAG_SCREEN, "shouldShowRationale = true (user denied previously)")
            Text("현재 위치를 확인하려면 위치 권한이 필요합니다.")
        }
    }
}