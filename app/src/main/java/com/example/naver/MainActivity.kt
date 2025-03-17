package com.example.naver

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import com.example.naver.R
import com.example.naver.BuildConfig
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var searchResultsView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchInput: EditText
    private val searchResults = mutableListOf<Pair<String, LatLng>>()  // 검색 결과 저장
    private lateinit var locationInfoTextView: TextView //위도 경도 표시


    private val clientId = BuildConfig.NAVER_CLIENT_ID
    private val clientSecret = BuildConfig.NAVER_CLIENT_SECRET

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 위치 소스 초기화
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // 네이버 지도 SDK 초기화
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(clientId)

        // MapView 초기화
        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // 위치 권한 요청
        requestLocationPermission()

        // 위치 제공자 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 위치 업데이트 콜백 설정
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateCurrentLocation(location)
                }
            }
        }

        // 🔹 검색창 입력 시 자동으로 검색 실행
        searchInput = findViewById(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    searchLocation(query)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        searchResultsView = findViewById(R.id.search_results)
        searchResultsView.layoutManager = LinearLayoutManager(this)

        searchAdapter = SearchAdapter(searchResults) { latLng ->
            moveToLocation(latLng.latitude, latLng.longitude)
            searchResultsView.visibility = View.GONE  // 선택 후 리스트 숨김
        }
        searchResultsView.adapter = searchAdapter

        val locationBackButton: Button = findViewById(R.id.locationback_button)
        locationBackButton.setOnClickListener {
            moveToCurrentLocation()
        }

        locationInfoTextView = findViewById(R.id.location_info)

    }


    private fun moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    Log.d("CURRENT_LOCATION", "현재 위치 - 위도: ${location.latitude}, 경도: ${location.longitude}")
                    naverMap.moveCamera(CameraUpdate.scrollTo(latLng)) // 현재 위치로 이동
                    naverMap.locationOverlay.position = latLng // 위치 오버레이 업데이트

                    //기존 마커가 있으면 위치만 변경 (새로 만들지 않음)
                    if (currentLocationMarker == null) {
                        currentLocationMarker = Marker().apply {
                            position = latLng
                            map = naverMap
                            captionText = "현재 위치"
                            captionTextSize = 16f
                            iconTintColor = ContextCompat.getColor(this@MainActivity, R.color.green)

                            onClickListener = Overlay.OnClickListener {
                                isCurrentedMarkClicked = true
                                changeMarkColor(this)
                                showLocationInfoOnScreen(location.latitude, location.longitude)
                                true
                            }
                        }
                    } else {
                        //기존 마커 위치만 업데이트
                        currentLocationMarker!!.position = latLng
                    }

                    //위치 업데이트 다시 시작
                    resumeLocationUpdates()
                } else {
                    Log.e("LOCATION_ERROR", "현재 위치를 가져올 수 없습니다.")
                }
            }
        } else {
            Log.e("PERMISSION_ERROR", "위치 권한이 없습니다.")
        }
    }




    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // 5초마다 위치 업데이트
            fastestInterval = 2000 // 최소 2초 간격으로 업데이트
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private var currentLocationMarker: Marker? = null  // 현재 위치 마커
    private var isCurrentedMarkClicked = false

    // 현재 위치를 업데이트할 때 마커 추가
    private fun updateCurrentLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        //naverMap.moveCamera(CameraUpdate.scrollTo(latLng))
        naverMap.locationOverlay.position = latLng

        // 🔹 기존 마커 삭제 (중복 방지)
        currentLocationMarker?.map = null

        // 🔹 새 마커 추가 (현재 위치)
        currentLocationMarker = Marker().apply {
            position = latLng
            map = naverMap
            //captionText = "현재위치"
            iconTintColor = ContextCompat.getColor(this@MainActivity,R.color.green)

            if(isCurrentedMarkClicked) {
                iconTintColor = ContextCompat.getColor(this@MainActivity,R.color.purple_700)
            } else {
                iconTintColor = ContextCompat.getColor(this@MainActivity,R.color.green)
            }

            onClickListener = Overlay.OnClickListener {
                isCurrentedMarkClicked = true
                changeMarkColor(this)
                showLocationInfoOnScreen(location.latitude,location.longitude)
                true
            }

        }
        if (!markers.contains(currentLocationMarker)) {
            markers.add(currentLocationMarker!!)
        }
    }




    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //사용자가 이전에 거부한 경우, 설명 다이얼로그 표시
                AlertDialog.Builder(this)
                    .setTitle("위치 권한 필요")
                    .setMessage("현재 위치를 가져오려면 위치 권한이 필요합니다.")
                    .setPositiveButton("권한 요청") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                            LOCATION_PERMISSION_REQUEST_CODE
                        )
                    }
                    .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
                    .show()
            } else {
                // 처음 요청하거나, "다시 묻지 않기"를 체크하지 않은 경우
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }


    private fun searchLocation(query: String) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=$encodedQuery"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("X-NCP-APIGW-API-KEY-ID", clientId)
            .addHeader("X-NCP-APIGW-API-KEY", clientSecret)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "API 요청 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("API_RESPONSE", "응답: $responseBody")

                responseBody?.let { jsonResponse ->
                    try {
                        val jsonObject = JSONObject(jsonResponse)
                        val addresses = jsonObject.getJSONArray("addresses")

                        if (addresses.length() == 0) {
                            Log.e("API_ERROR", "검색 결과 없음!")
                            return
                        }

                        searchResults.clear()
                        for (i in 0 until addresses.length()) {
                            val address = addresses.getJSONObject(i)
                            val roadAddress = address.optString("roadAddress", "주소 없음")
                            val latitude = address.getString("y").toDouble()
                            val longitude = address.getString("x").toDouble()
                            searchResults.add(Pair(roadAddress, LatLng(latitude, longitude)))
                        }

                        runOnUiThread {
                            searchAdapter.notifyDataSetChanged()
                            searchResultsView.visibility = if (searchResults.isNotEmpty()) View.VISIBLE else View.GONE
                        }
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "JSON 파싱 오류: ${e.message}")
                    }
                }
            }
        })
    }

    private fun moveToLocation(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)

        // 지도 이동
        naverMap.moveCamera(CameraUpdate.scrollTo(latLng))

        // 기존 마커 제거 후 새 마커 추가
        val marker = Marker()
        marker.position = latLng
        marker.map = naverMap

        // 🔹 위치 업데이트 중지 (검색 결과를 유지하기 위해)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 사용자가 다시 현재 위치 버튼을 누르면 위치 업데이트 재개
    private fun resumeLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 2000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    // 🔹 네이버 지도에서 사용자가 클릭하면 위치 업데이트 재개
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // 현재 위치 활성화
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true

        // 위치 오버레이 활성화
        val locationOverlay: LocationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true

        // 앱 실행 시 현재 위치를 기본으로 설정하기
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    Log.d("CURRENT_LOCATION", "현재 위치 - 위도: ${location.latitude}, 경도: ${location.longitude}") // ✅ 로그 출력
                    naverMap.moveCamera(CameraUpdate.scrollTo(latLng)) // 현재 위치로 지도 이동
                    locationOverlay.position = latLng // 현재 위치를 오버레이로 표시
                } else {
                    Log.e("LOCATION_ERROR", "초기 위치를 가져올 수 없습니다.")
                }
            }
        } else {
            Log.e("PERMISSION_ERROR", "위치 권한이 없습니다.")
        }

        // 위치 업데이트 시작
        startLocationUpdates()

        //지도 클릭 시 마커 추가
        naverMap.setOnMapClickListener { _, coord ->
            Log.d("MAP_CLICK", "지도 클릭 - 위도: ${coord.latitude}, 경도: ${coord.longitude}")
            addMarkerAtLocation(coord.latitude, coord.longitude) // 🔹 클릭한 위치에 마커 추가
        }
    }

    private val markers = mutableListOf<Marker>() // 🔹 마커 리스트 (중복 방지)
    private var markerCount =1
    private var selectedMarker: Marker? = null // 🔹 현재 선택된 마커를 저장하는 변수


    private fun addMarkerAtLocation(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)

        val marker = Marker().apply {
            position = latLng
            map = naverMap  // 네이버 지도에 마커 추가
            captionText = markerCount.toString()  // 마커 위에 숫자 표시
            captionTextSize = 16f
            captionColor = ContextCompat.getColor(this@MainActivity, R.color.black)
            iconTintColor = ContextCompat.getColor(this@MainActivity,R.color.green)

            // 🔹 마커 클릭 이벤트 설정 (중요!)
            onClickListener = Overlay.OnClickListener {
                changeMarkColor(this)
                showLocationInfoOnScreen(latitude, longitude)
                true  // 이벤트 소비
            }
        }

        markers.add(marker)
        markerCount++
        Log.d("MARKER", "마커 추가 - 번호: ${markerCount - 1}, 위도: $latitude, 경도: $longitude")
    }



    private fun changeMarkColor(newSelectedMarker: Marker) {
        for (marker in markers) {
            if (marker == currentLocationMarker) {
                //다른 마커를 클릭하면 현재 위치 마커 색상을 초록색으로 변경
                marker.iconTintColor = ContextCompat.getColor(this, R.color.green)
                isCurrentedMarkClicked = false  //다른 마커를 선택하면 현재 위치 마커 선택 해제
            } else {
                marker.iconTintColor = ContextCompat.getColor(this, R.color.green)  // 기본 초록색
            }
        }

        // 🔹 새로운 선택된 마커를 보라색으로 변경
        newSelectedMarker.iconTintColor = ContextCompat.getColor(this, R.color.purple_500)
        selectedMarker = newSelectedMarker
    }



    private fun showLocationInfoOnScreen(latitude: Double, longitude: Double) {
//        val formattedLatitude = String.format("%.4f",latitude); //
//        val formattedLongitude = String.format("%.4f",longitude);


            locationInfoTextView.text = "위도: $latitude\n경도: $longitude"
            locationInfoTextView.visibility = View.VISIBLE  // 화면에 표시


    }


    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onStop() { super.onStop(); fusedLocationClient.removeLocationUpdates(locationCallback); mapView.onStop() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
