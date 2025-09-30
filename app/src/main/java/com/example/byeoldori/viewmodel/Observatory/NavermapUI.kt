package com.example.byeoldori.viewmodel.Observatory

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.R
import com.example.byeoldori.data.model.dto.ObservationSite
import com.example.byeoldori.domain.Observatory.MarkerInfo
import com.example.byeoldori.domain.Observatory.ObservatoryType
import com.example.byeoldori.ui.components.observatory.CurrentLocationButton
import com.example.byeoldori.viewmodel.NaverMapViewModel
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private const val TAG_UI = "NavermapUI"

@Composable
fun NaverMapWithSearchUI(
    modifier: Modifier = Modifier,
    viewModel: NaverMapViewModel = viewModel(),
    searchQuery: String,
    onSearchRequested: (String)->Unit,
    onLatLngUpdated: (LatLng)->Unit,
    onAddressUpdated: (String)->Unit,
    searchTrigger: Int,
    showOverlay: Boolean,
    onMarkerClick: (MarkerInfo) -> Unit,
    onCurrentLocated: (Double, Double) -> Unit,
    sites: List<ObservationSite> //api에서 받은 관측지 리스트
) {
    //이 부분들은 UI에 종속적인 객체(viewModel사용 안함)
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapView = remember { MapView(context) }
    var selectedMarker by remember { mutableStateOf<Marker?>(null) }
    var lightOverlay by remember { mutableStateOf<com.naver.maps.map.overlay.GroundOverlay?>(null) }
    var naverMapObj by remember { mutableStateOf<NaverMap?>(null) }
    var currentCoordinate by remember { mutableStateOf("127.0,37.0") }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    val userMarker = remember { Marker() }
    var userMarkerPlaced by remember { mutableStateOf(false) }


    val markers = remember { mutableStateListOf<Marker>() }

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
                    onCurrentLocated(it.latitude, it.longitude) //사용자의 현재 위도,경도를 초기값으로 설정
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
                        selectedMarker?.setIconTintColor(Color.BLACK)    // ✅
                        marker.setIconTintColor(Color.RED)
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

    // 지도 뷰
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { naverMap ->
                        naverMapObj = naverMap
                        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow

                        // 지도 초기 진입 시 오버레이 상태 반영
                        if (showOverlay) {
                            lightOverlay = addLightPollutionOverlay(context, naverMap)
                        } else {
                            lightOverlay?.map = null
                            lightOverlay = null
                        }

                        // 준비 직후 현재 위치로 카메라 이동(가능하면)
                        val fineGranted = ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val coarseGranted = ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (fineGranted || coarseGranted) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    val lat = location.latitude
                                    val lon = location.longitude

                                    naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(location.latitude, location.longitude)))
                                    onCurrentLocated(location.latitude, location.longitude)
                                    if (!userMarkerPlaced) {
                                        userMarker.position = LatLng(lat, lon)
                                        userMarker.map = naverMap
                                        userMarkerPlaced = true
                                    } else {
                                        userMarker.position = LatLng(lat, lon) // 이후엔 위치만 갱신
                                    }
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 현재 위치 버튼
        CurrentLocationButton(
            context = context,
            mapView = mapView,
            fusedLocationClient = fusedLocationClient,
            onLocated = { lat, lon ->
                Log.d(TAG_UI, "onLocated from button: lat=$lat lon=$lon")
                onCurrentLocated(lat, lon)
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = (-10).dp)
                .padding(16.dp)
        )
    }

    //sites 또는 지도 준비 상태가 바뀔 때마다 마커 갱신
    LaunchedEffect(sites, naverMapObj) {
        val naverMap = naverMapObj ?: return@LaunchedEffect

        // 기존 마커 제거
        markers.forEach { it.map = null }
        markers.clear()

        // 새 마커 추가
        sites.forEach { site ->
            Marker().apply {
                position = LatLng(site.latitude, site.longitude)
                captionText = site.name
                // 타입 정보가 아직 없으므로 기본 아이콘 사용(필요시 타입에 따라 분기)
                icon = OverlayImage.fromResource(R.drawable.ic_marker_p)
                map = naverMap

                setOnClickListener {
                    // 클릭 시 주소 역지오코딩(비동기) → MarkerInfo로 콜백
                    scope.launch {
                        val addr = withContext(Dispatchers.IO) {
                            try {
                                Log.d(TAG_UI, "Reverse geocoding start (Naver): ${site.name}")

                                val road = reverseAddressRoadNaver(site.latitude, site.longitude)
                                val result = if (road.isNotBlank()) {
                                    Log.d(TAG_UI, "Reverse geocoding success (Naver): $road")
                                    road
                                } else {
                                    val fallback = getAddressFromLatLng(geocoder, site.latitude, site.longitude)
                                    Log.d(TAG_UI, "Fallback geocoder success (Android): $fallback")
                                    fallback
                                }

                                // 블록의 마지막 표현식은 반드시 String!
                                result
                            } catch (e: Exception) {
                                Log.e(TAG_UI, "Geocoding failed: ${e.message}", e)
                                "주소를 불러오지 못했습니다."
                            }
                        }
                        onMarkerClick(
                            MarkerInfo(
                                name = site.name,
                                type = ObservatoryType.GENERAL, // 기본값
                                address = addr,
                                drawableRes = R.drawable.img_dummy,
                                reviewCount = 0,
                                likeCount = 0,
                                rating = 0f,
                                suitability = 0,
                                latitude = site.latitude,
                                longitude = site.longitude
                            )
                        )
                    }
                    true
                }
            }.also { markers += it }
        }
    }
}