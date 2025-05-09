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
import com.example.byeoldori.viewmodel.NaverMapViewModel
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.*
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NaverMapWithSearchUI(
    modifier: Modifier = Modifier,
    viewModel: NaverMapViewModel = viewModel(),
    searchQuery: String,
    onSearchRequested: (String)->Unit,
    onLatLngUpdated: (LatLng)->Unit,
    onAddressUpdated: (String)->Unit,
    searchTrigger: Int,
    showOverlay: Boolean
) {
    //이 부분들은 UI에 종속적인 객체(viewModel사용 안함)
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapView = remember { MapView(context) }
    var selectedMarker by remember { mutableStateOf<Marker?>(null) }
    var lightOverlay by remember { mutableStateOf<com.naver.maps.map.overlay.GroundOverlay?>(null) }
    var naverMapObj by remember { mutableStateOf<NaverMap?>(null) }
    var currentCoordinate by remember { mutableStateOf("127.0,37.0") }

    //ViewModel사용
    val selectedLatLng  by viewModel.selectedLatLng.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()


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
                    onMarkerUpdated = { marker ->
                        selectedMarker?.iconTintColor = Color.BLACK //이전마커 색 초기화
                        marker.iconTintColor = Color.RED
                        selectedMarker = marker
                    },
                    onAddressUpdated = { viewModel.updateSelectedAddress(it) },
                    onLatLngUpdated = { viewModel.updateSelectedLatLng(it) }
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
                                    iconTintColor = Color.BLACK

                                    setOnClickListener {
                                        selectedMarker?.iconTintColor = Color.BLACK
                                        this.iconTintColor = Color.RED
                                        selectedMarker = this

                                        viewModel.updateSelectedLatLng(currentLatLng)
                                        val address = getAddressFromLatLng(
                                            geocoder,
                                            currentLatLng.latitude,
                                            currentLatLng.longitude
                                        )
                                        viewModel.updateSelectedAddress(address)
                                        true
                                    }
                                }
                            }
                        }

                        naverMap.setOnMapClickListener { point, coord ->
                            // 지도 클릭하니까 새 마커 생성
                            Marker().apply {
                                position = coord
                                map = naverMap
                                iconTintColor = Color.BLACK

                                setOnClickListener {
                                    selectedMarker?.iconTintColor = Color.BLACK
                                    this.iconTintColor = Color.RED
                                    selectedMarker = this

                                    viewModel.updateSelectedLatLng(coord)
                                    val address = getAddressFromLatLng(
                                        geocoder,
                                        coord.latitude,
                                        coord.longitude
                                    )
                                    viewModel.updateSelectedAddress(address)
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
                    LocationInfoBox(
                        latLng = latLng,
                        address = address,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x=20.dp, y=120.dp)
                    )
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