package com.example.byeoldori.ui.screen.Observatory

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.*
import java.util.Locale

@Composable
fun NaverMapWithSearchUI(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchRequested: (String)->Unit,
    onLatLngUpdated: (LatLng)->Unit,
    onAddressUpdated: (String)->Unit,
    searchTrigger: Int,
    showOverlay: Boolean
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapView = remember { MapView(context) }
    var currentCoordinate by remember { mutableStateOf("127.0,37.0") }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var selectedMarker by remember { mutableStateOf<Marker?>(null) }
    var lightOverlay by remember { mutableStateOf<com.naver.maps.map.overlay.GroundOverlay?>(null) }
    var naverMapObj by remember { mutableStateOf<NaverMap?>(null) }


    LaunchedEffect(showOverlay) {
        naverMapObj?.let { map -> //naverMapObj가 있는 경우만 동작
            if (showOverlay) {
                lightOverlay = addLightPollutionOverlay(context,map)
            } else {
                lightOverlay?.map = null
            }
        }
    }

    LaunchedEffect(Unit) { //초기에 실행
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentCoordinate = "${it.longitude},${it.latitude}" //사용자의 현재 위도,경도를 초기값으로 설정
                }
            }
        }
    }

    LaunchedEffect(searchTrigger) {
        if (searchQuery.isNotBlank()) {
            mapView.getMapAsync { naverMap ->
                searchAndMoveToLocation(
                    context, searchQuery, naverMap, mapView, selectedMarker,
                    onMarkerUpdated = { selectedMarker = it },
                    onAddressUpdated = onAddressUpdated,
                    onLatLngUpdated = { latLng ->
                        selectedLatLng = latLng
                        onLatLngUpdated(latLng)
                    }
                )
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
                        naverMapObj = naverMap
                        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow

                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) return@getMapAsync

                        if (showOverlay) {
                            lightOverlay = addLightPollutionOverlay(context, naverMap)
                        } else {
                            lightOverlay?.map = null
                        }

                        val geocoder = Geocoder(context, Locale.getDefault()) //주소->좌표

                        //현재 위치를 지도에 표시
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                val currentLatLng = LatLng(location.latitude, location.longitude)
                                naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng))

                                Marker().apply {
                                    position = currentLatLng
                                    map = naverMap

                                    setOnClickListener {
                                        //마커 클릭했을 때 위도/경도로부터 주소 검색
                                        selectedAddress = getAddressFromLatLng(
                                            geocoder,
                                            currentLatLng.latitude,
                                            currentLatLng.longitude
                                        )
                                        selectedLatLng = currentLatLng //클릭한 위치의 위도,경도 저장
                                        selectedMarker?.iconTintColor = Color.BLACK // 이전 선택 마커 복구
                                        this.iconTintColor = Color.RED // 현재 선택한 마커 빨간색
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
                                    selectedLatLng = coord
                                    selectedAddress = getAddressFromLatLng(
                                        geocoder,
                                        coord.latitude,
                                        coord.longitude
                                    )
                                    selectedMarker?.iconTintColor = Color.BLACK
                                    this.iconTintColor = Color.RED
                                    selectedMarker = this

                                    true
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize() //박스가 전체화면 채우도록
        )

        selectedLatLng?.let { latLng ->
            selectedAddress?.let { address ->
                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                    LocationInfoBox(latLng = latLng, address = address)
                }
            }
        }

        CurrentLocationButton(
            context = context,
            mapView = mapView,
            fusedLocationClient = fusedLocationClient,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}



