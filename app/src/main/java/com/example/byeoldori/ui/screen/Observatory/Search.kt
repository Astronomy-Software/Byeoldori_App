package com.example.byeoldori.ui.screen.Observatory

import android.content.Context
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import java.util.Locale
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp


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
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .padding(top = 0.dp, start = 0.dp)
            .fillMaxWidth(0.7f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        TextField(
            value = searchQuery, //사용자가 입력한 현재 값
            onValueChange = onSearchQueryChange, //사용자가 입력을 변경할 때마다 searchQuery 값 업데이트
            placeholder = {
                Text("검색할 내용을 입력해주세요",
                    color = Color.White,
                    fontSize = 12.sp
                )
            },
            singleLine = true, //한 줄만 입력 가능
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min=40.dp),

            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF241860),
                unfocusedContainerColor = Color(0xFF241860),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(color = Color.White), // 🔥 입력 글씨 색상 지정
            leadingIcon = {
                Icon(Icons.Default.Search,
                    contentDescription = "검색",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search), // ✅ 검색 버튼으로 설정
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide() // 키보드 닫기
                    onSearchClick()            // 🔥 검색 실행
                }
            )
        )
    }
}