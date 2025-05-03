package com.example.byeoldori.ui.screen.Observatory

import android.content.Context
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import java.util.Locale

fun searchAndMoveToLocation(
    context: Context,
    query: String,
    naverMap: NaverMap,
    mapView: MapView,
    selectedMarker: Marker?,
    onMarkerUpdated: (Marker) -> Unit,

    onLatLngUpdated: (LatLng) -> Unit,
    onAddressUpdated: (String) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocationName(query, 1) //주소->좌표

    if (!addresses.isNullOrEmpty()) {
        val address = addresses[0] //검색 결과 중에 첫번째 주소 객체
        val latLng = LatLng(address.latitude, address.longitude) //위도,경도 객체 생성

        naverMap.moveCamera(CameraUpdate.scrollTo(latLng))
        selectedMarker?.iconTintColor = android.graphics.Color.BLACK

        val marker = Marker().apply {
            position = latLng
            map = naverMap
            iconTintColor = android.graphics.Color.RED

            setOnClickListener {
                onMarkerUpdated(this)
                onLatLngUpdated(latLng)
                onAddressUpdated(getAddressFromLatLng(geocoder, latLng.latitude, latLng.longitude))
                true
            }
        }
        onMarkerUpdated(marker)
        onLatLngUpdated(latLng)
        onAddressUpdated(getAddressFromLatLng(geocoder, latLng.latitude, latLng.longitude))
    } else {
        Toast.makeText(context, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun SearchBox(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = 20.dp, start = 16.dp)
            .fillMaxWidth(0.4f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        TextField(
            value = searchQuery, //사용자가 입력한 현재 값
            onValueChange = onSearchQueryChange, //사용자가 입력을 변경할 때마다 searchQuery 값 업데이트
            placeholder = { Text("주소 또는 장소를 입력하세요") },
            singleLine = true, //한 줄만 입력가능
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.White
            ),
            trailingIcon = {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "검색")
                }
            }
        )
    }
}