package com.example.byeoldori.ui.screen.Observatory

import android.Manifest
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

@Composable
fun NavermapScreen(
    searchQuery: String,
    onSearch: (String) -> Unit,
    showOverlay: Boolean,
    searchTrigger: Int,
    onLatLngUpdated: (LatLng)->Unit,     // 추가
    onAddressUpdated: (String)->Unit,
    onMarkerClick: (MarkerInfo) -> Unit,  // 추가
    modifier: Modifier = Modifier
) {
    val navViewModel: NavigationViewModel = viewModel()

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
                onMarkerClick       = onMarkerClick
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(onPermissionGranted: @Composable () -> Unit) {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    when {
        permissionState.status.isGranted -> {
            onPermissionGranted()
        }
        permissionState.status.shouldShowRationale -> {
            Text("이 앱은 현재 위치를 확인하기 위해 위치 권한이 필요합니다.")
        }
        else -> {
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
        }
    }
}