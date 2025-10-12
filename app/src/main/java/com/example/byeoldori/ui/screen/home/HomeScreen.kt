package com.example.byeoldori.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.components.observatory.CurrentWeatherSection
import com.example.byeoldori.ui.home.GetLocation
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.viewmodel.Observatory.*

@Composable
fun HomeScreen(
    vm: NaverMapViewModel = hiltViewModel()
) {

    val locationState = GetLocation(vm)
    var suitability by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text("홈화면이에요", fontSize = 28.sp, color = TextHighlight)
            Spacer(Modifier.height(16.dp))
        }
        item {
            if (locationState.lat != null && locationState.lon != null) {
                Text("위도(Lat): ${"%.5f".format(locationState.lat)}", fontSize = 16.sp, color = TextHighlight)
                Text("경도(Lon): ${"%.5f".format(locationState.lon)}", fontSize = 16.sp, color = TextHighlight)
            }
            if (locationState.address.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("주소: ${locationState.address}", fontSize = 16.sp, color = TextHighlight)
            }
            Spacer(Modifier.height(40.dp))
        }

        item {
            when {
                locationState.isLoading -> {
                    CircularProgressIndicator()
                    Text("현재 위치를 확인하는 중..")
                }
                locationState.lat != null && locationState.lon != null -> {
                    CurrentWeatherSection(
                        lat = locationState.lat!!,
                        lon = locationState.lon!!,
                        onSuitabilityChange = { suitability = it } // 필요하면 상태로 보관
                    )
                }
                else -> {
                    Text("현재 날씨 정보를 가져올 수 없습니다.")
                }
            }
        }
    }
}

