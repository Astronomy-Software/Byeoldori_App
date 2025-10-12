package com.example.byeoldori.ui.home

import android.location.Geocoder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.screen.Observatory.RequestLocationPermission
import com.example.byeoldori.viewmodel.Observatory.NaverMapViewModel
import com.example.byeoldori.viewmodel.Observatory.getAddressFromLatLng
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class LocationAddressState(
    val lat: Double?,
    val lon: Double?,
    val address: String,
    val isLoading: Boolean
)

@Composable
fun GetLocation(
    vm: NaverMapViewModel = hiltViewModel()
): LocationAddressState {
    val context = LocalContext.current
    //현재 위치를 가져오거나, 위치 업데이트를 요청할 수 있는 클라이언트 객체(remember로 한번만 생성해서 상태기억)
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }
    var lat by rememberSaveable { mutableStateOf<Double?>(null) }
    var lon by rememberSaveable { mutableStateOf<Double?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var address by rememberSaveable { mutableStateOf("") }

    //위치 정보 권한 허용 여부
    RequestLocationPermission {
        LaunchedEffect(Unit) {
            isLoading = true
            //고정밀 위치 정보에 접근할 수 있는 권한
            val fine = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION //정밀 위치(GPS센서)
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            //대략적인 위치 정보에 접근할 수 있는 권한
            val coarse = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION //대략 위치(Wi-Fi, 셀룰러 기지국 정보)
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if(fine || coarse) {
                fused.lastLocation.addOnSuccessListener { loc ->
                    lat = loc?.latitude
                    lon = loc?.longitude
                    isLoading = false
                }.addOnFailureListener { isLoading = false }
            } else {
                isLoading = false
            }
        }
    }

    LaunchedEffect(lat, lon) {
        val la = lat; val lo = lon
        if(la != null && lo != null) {
            address = withContext(Dispatchers.IO) {
                val naver = kotlin.runCatching { vm.reverseAddressRoad(la,lo) }.getOrNull()
                if(!naver.isNullOrBlank()) {
                    naver
                } else {
                    kotlin.runCatching {
                        getAddressFromLatLng(geocoder = Geocoder(context, Locale.KOREA), lat = la, lng = lo)
                    }.getOrDefault("사용자의 주소를 찾을 수 없습니다.")
                }
            }
        }
    }
    return LocationAddressState(
        lat = lat,
        lon = lon,
        address = address,
        isLoading = isLoading
    )
}