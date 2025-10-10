package com.example.byeoldori.viewmodel.Observatory

import android.content.Context
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.data.repository.NavermapRepository
import com.example.byeoldori.domain.Observatory.*
import com.example.byeoldori.ui.theme.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.runBlocking
import java.util.Locale

fun searchAndMoveToLocation(
    context: Context,
    query: String,
    naverMap: NaverMap,
    mapView: MapView,
    selectedMarker: Marker?,
    onMarkerUpdated: (Marker) -> Unit,
    onLatLngUpdated: (LatLng) -> Unit,
    onAddressUpdated: (String) -> Unit,
    onMarkerClick: (MarkerInfo) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    val repo = NavermapRepository()
    val addresses = geocoder.getFromLocationName(query, 1) //주소->좌표

    if (!addresses.isNullOrEmpty()) {
        val address = addresses[0] //검색 결과 중에 첫번째 주소 객체
        val latLng = LatLng(address.latitude, address.longitude) //위도,경도 객체 생성

        val addr = runBlocking {
            try {
                val naverAddr = repo.reverseAddressRoad(latLng.latitude, latLng.longitude)
                if (naverAddr.isNotBlank()) naverAddr
                else getAddressFromLatLng(geocoder, latLng.latitude, latLng.longitude)
            } catch (e: Exception) {
                getAddressFromLatLng(geocoder, latLng.latitude, latLng.longitude)
            }
        }
        onAddressUpdated(addr)

        naverMap.moveCamera(CameraUpdate.scrollTo(latLng))
        selectedMarker?.iconTintColor = android.graphics.Color.BLACK

        val marker = Marker().apply {
            position = latLng
            map = naverMap

            setOnClickListener {
                onMarkerUpdated(this)

                val updatedAddr = runBlocking {
                    try {
                        val naverAddr = repo.reverseAddressRoad(latLng.latitude, latLng.longitude)
                        if (naverAddr.isNotBlank()) naverAddr
                        else getAddressFromLatLng(geocoder, latLng.latitude, latLng.longitude)
                    } catch (e: Exception) {
                        getAddressFromLatLng(geocoder, latLng.latitude, latLng.longitude)
                    }
                }

                //주소 기반으로 클릭 시마다 이름 재계산
                val updatedFirst = Regex("\\(([^)]+)\\)").find(updatedAddr)?.groupValues?.get(1)?.trim()
                val updatedSecond = if (updatedFirst == null) {
                    val parts = updatedAddr.split(" ")
                    val idx = parts.indexOfLast { it.endsWith("구") || it.endsWith("군") || it.endsWith("시") }
                    parts.drop(idx + 1).takeLast(4).joinToString(" ").ifBlank { "선택한 위치" }
                } else null
                val updatedDisplayName = updatedFirst ?: updatedSecond ?: "선택한 위치"

                val info = MarkerInfo(
                    name = updatedDisplayName,
                    type = ObservatoryType.GENERAL,
                    address = updatedAddr,
                    drawableRes = R.drawable.img_dummy,
                    reviewCount = 0,
                    likeCount = 0,
                    rating = 0f,
                    suitability = 0,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                )

                //순서: Popup 갱신 → 상태 업데이트 → Info 전달
                onLatLngUpdated(latLng)
                onAddressUpdated(updatedAddr)
                onMarkerClick(info)

                true
            }
        }
        onMarkerUpdated(marker)
        onLatLngUpdated(latLng)
        onAddressUpdated(addr)
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
                    color = TextHighlight,
                    fontSize = 12.sp
                )
            },
            //singleLine = true, //한 줄만 입력 가능
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min=40.dp),

            colors = TextFieldDefaults.colors(
                focusedContainerColor = Blue800,
                unfocusedContainerColor = Blue800,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = TextHighlight
            ),
            textStyle = LocalTextStyle.current.copy(color = TextHighlight), //입력 글씨 색상 지정
            leadingIcon = {
                Icon(Icons.Default.Search,
                    contentDescription = "검색",
                    tint = TextHighlight,
                    modifier = Modifier.size(18.dp)
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search), //검색 버튼으로 설정
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide() // 키보드 닫기
                    onSearchClick()            //검색 실행
                }
            )
        )
    }
}