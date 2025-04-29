package com.example.byeoldori.ui.screen.Navermap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.naver.maps.map.LocationTrackingMode
import java.util.Locale
import android.graphics.Color as AndroidColor





@Composable
fun NavermapScreen(onBack: () -> Unit) {
    RequestLocationPermission {
        Box(modifier = Modifier.fillMaxSize()) {
            // 지도 및 검색 UI
            NaverMapWithSearchUI(modifier = Modifier.fillMaxSize())

            // 오른쪽 상단 돌아가기 버튼
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
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

@Composable
fun NaverMapWithSearchUI(modifier: Modifier) {

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapView = remember { MapView(context) }
    var currentCoordinate by remember { mutableStateOf("127.0,37.0") }

    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var selectedMarker by remember { mutableStateOf<Marker?>(null) }
    var searchQuery by remember { mutableStateOf("") }





    fun moveToCurrentLocation() {
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
                            this.iconTintColor = AndroidColor.BLACK
                        }
                    }
                }
            }
        }
    }

    fun searchAndMoveToLocation(query: String, naverMap: NaverMap) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(query, 1)

        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            val latLng = LatLng(address.latitude, address.longitude)

            naverMap.moveCamera(CameraUpdate.scrollTo(latLng))

            selectedMarker?.iconTintColor = AndroidColor.BLACK

            val marker = Marker().apply {
                position = latLng
                map = naverMap
                iconTintColor = AndroidColor.BLACK
            }
            selectedMarker = marker

            selectedLatLng = latLng
            selectedAddress = address.getAddressLine(0) ?: "주소를 찾을 수 없습니다."
        } else {
            Toast.makeText(context, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentCoordinate = "${it.longitude},${it.latitude}"
                }
            }
        }
    }

    DisposableEffect(Unit) {
        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { naverMap ->
                        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow

                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) return@getMapAsync

                        val geocoder = Geocoder(context, Locale.getDefault())


                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                val currentLatLng = LatLng(location.latitude, location.longitude)
                                naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng))

                                Marker().apply {
                                    position = currentLatLng
                                    map = naverMap

                                    setOnClickListener {
                                        val addresses = geocoder.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 1)
                                        val address = addresses?.firstOrNull()

                                        val fullAddress = address?.getAddressLine(0) ?: "주소를 찾을 수 없습니다."
                                        val featureName = address?.featureName ?: ""

                                        val addressWithPlace = if (featureName.isNotBlank() && !fullAddress.contains(featureName)) {
                                            "$fullAddress ($featureName)"
                                        } else {
                                            fullAddress
                                        }

                                        selectedLatLng = currentLatLng
                                        selectedAddress = addressWithPlace
                                        selectedMarker?.iconTintColor = AndroidColor.parseColor("#006400") // 이전 선택 마커 복구
                                        this.iconTintColor = AndroidColor.RED // 현재 선택한 마커 빨간색
                                        selectedMarker = this // 현재 선택된 마커 갱신
                                        true
                                    }
                                }
                            }
                        }

                        naverMap.setOnMapClickListener { point, coord ->
                            // 지도 클릭이니까 새 마커 생성
                            Marker().apply {
                                position = coord
                                map = naverMap

                                setOnClickListener {
                                    val addresses = geocoder.getFromLocation(coord.latitude, coord.longitude, 1)
                                    val address = addresses?.firstOrNull()

                                    val fullAddress = address?.getAddressLine(0) ?: "주소를 찾을 수 없습니다."
                                    val featureName = address?.featureName ?: ""

                                    val addressWithPlace = if (featureName.isNotBlank() && !fullAddress.contains(featureName)) {
                                        "$fullAddress ($featureName)"
                                    } else {
                                        fullAddress
                                    }

                                    selectedLatLng = coord
                                    selectedAddress = addressWithPlace

                                    selectedMarker?.iconTintColor = AndroidColor.BLACK
                                    this.iconTintColor = AndroidColor.RED
                                    selectedMarker = this

                                    true
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        selectedLatLng?.let { latLng ->
            selectedAddress?.let { address ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-40).dp)
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
        }

        Button(
            onClick = {
                mapView.getMapAsync { naverMap ->
                    moveToCurrentLocation()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("현재 위치")
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .fillMaxWidth(0.4f) // 화면의 약 40% 차지
                .clip(RoundedCornerShape(16.dp))
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("주소 또는 장소를 입력하세요") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        mapView.getMapAsync { naverMap ->
                            searchAndMoveToLocation(searchQuery, naverMap)
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }
                }
            )
        }
    }
}








