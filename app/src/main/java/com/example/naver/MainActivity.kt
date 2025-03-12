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
    }

    private fun moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    naverMap.moveCamera(CameraUpdate.scrollTo(latLng)) // 현재 위치로 이동
                    naverMap.locationOverlay.position = latLng // 위치 오버레이 업데이트

                    // 🔹 현재 위치 마커 추가
                    currentLocationMarker?.map = null // 기존 마커 삭제
                    currentLocationMarker = Marker().apply {
                        position = latLng
                        map = naverMap
                    }

                    // 🔹 위치 업데이트 다시 시작
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

    // 현재 위치를 업데이트할 때 마커 추가
    private fun updateCurrentLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        naverMap.moveCamera(CameraUpdate.scrollTo(latLng))
        naverMap.locationOverlay.position = latLng

        // 🔹 기존 마커 삭제 (중복 방지)
        currentLocationMarker?.map = null

        // 🔹 새 마커 추가 (현재 위치)
        currentLocationMarker = Marker().apply {
            position = latLng
            map = naverMap
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
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

        // 실시간 위치 업데이트 시작
        startLocationUpdates()

        // 🔹 사용자가 맵을 클릭하면 위치 업데이트 다시 시작
        naverMap.setOnMapClickListener { _, _ ->
            resumeLocationUpdates()
        }
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
