package com.example.byeoldori.ui.screen.Observatory

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.dto.ObservationSite
import com.example.byeoldori.domain.Observatory.MarkerInfo
import com.example.byeoldori.viewmodel.Observatory.NaverMapWithSearchUI
import com.example.byeoldori.viewmodel.Observatory.ObservatoryMapViewModel
import com.example.byeoldori.viewmodel.UiState
import com.google.accompanist.permissions.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap

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
    onCurrentLocated: (Double, Double) -> Unit,
    vm: ObservatoryMapViewModel = hiltViewModel(),
    onMapClick: (LatLng) -> Unit = {},
    onMapReady: (NaverMap) -> Unit = {}
) {
    val state = vm.state.collectAsState().value

    RequestLocationPermission {
        Box(modifier = Modifier.fillMaxSize()) {
            when (state) {
                UiState.Idle ->
                    Text("관측지 정보를 불러오는 중입니다.")
                UiState.Loading ->
                    CircularProgressIndicator()

                is UiState.Success -> {
                    val sites = (state as UiState.Success<List<ObservationSite>>).data
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
                        onMapReady = onMapReady,
                        onCurrentLocated    = { lat, lon ->
                            Log.d(TAG_SCREEN, "onCurrentLocated from UI: lat=$lat lon=$lon")
                            onCurrentLocated(lat, lon) // 부모(ObservatoryScreen)로 그대로 전달
                        },
                        sites = sites
                    )
                }
                is UiState.Error -> {
                    val message = (state as UiState.Error).message
                    Text("관측지 정보를 불러오지 못했습니다: $message")
                }
            }
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