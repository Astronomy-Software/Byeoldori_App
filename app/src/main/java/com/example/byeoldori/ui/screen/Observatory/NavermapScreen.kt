package com.example.byeoldori.ui.screen.Observatory

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.viewmodel.AppScreen
import com.example.byeoldori.viewmodel.NavigationViewModel
import com.google.accompanist.permissions.*
import com.naver.maps.geometry.LatLng

@Composable
fun NavermapScreen(
    searchQuery: String,
    onSearch: (String) -> Unit,
    showOverlay: Boolean,
    searchTrigger: Int,
    onLatLngUpdated: (LatLng)->Unit,     // 추가
    onAddressUpdated: (String)->Unit,    // 추가
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
                showOverlay  = showOverlay
            )

            // 오른쪽 상단 돌아가기 버튼
            IconButton(
                onClick = {navViewModel.navigateTo(AppScreen.SkyMap)},
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top=20.dp, start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
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