package com.example.byeoldori.ui.components.observatory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Observatory.DailyForecast
import com.example.byeoldori.viewmodel.Observatory.WeatherViewModel
import com.example.byeoldori.viewmodel.UiState

@Composable
fun WeatherDailyPanel(
    lat: Double,
    lon: Double,
    viewModel: WeatherViewModel = hiltViewModel() //Hilt가 WeatherViewModel 객체를 만들어서 자동으로 넣어줌
) {
    val dailyState by viewModel.daily.collectAsState() //Compose State로 변환

    // 최초 진입 시 우암산 좌표로 불러오기
    LaunchedEffect(lat, lon) {
        viewModel.getDaily(lat, lon)  // ← 좌표 기반 API 호출
    }

    when(val state = dailyState) {
        UiState.Idle -> Text("날씨 정보를 불러오는 중입니다.")
        UiState.Loading -> {
            Box(Modifier.fillMaxWidth().height(120.dp)) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        is UiState.Success -> {
            val forecasts = state.data
            if(forecasts.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(120.dp)) {
                    Text("예보 데이터가 없습니다.", modifier = Modifier.align(Alignment.Center))
                }
            } else {
                DailyForecastListSection(forecasts = state.data)
            }
        }
        is UiState.Error -> Text("날씨 정보를 불러오지 못했습니다: ${state.message}")
    }
}

@Composable
fun DailyForecastListSection(forecasts: List<DailyForecast>) {
    // 전체를 감싸는 배경 박스
    Spacer(modifier = Modifier.height(15.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue800, RoundedCornerShape(15.dp))
            .padding(16.dp)  // 내부 전체 여백
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            forecasts.forEach {  forecast ->
                DailyForecastRow(forecast)
            }
        }
    }
}

@Composable
fun DailyForecastRow(forecast: DailyForecast) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 날짜
        Text(forecast.date, color = TextHighlight, fontSize = 18.sp, maxLines = 1,modifier = Modifier.weight(1f))

        // 강수 확률
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
            Image(painter = painterResource(id = R.drawable.ic_humidity), contentDescription = "강수",modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(forecast.precipitation, color = TextHighlight, fontSize = 12.sp)
        }

        // 오전 아이콘
        Image(
            painter = painterResource(id = getWeatherIconResId(forecast.amIcon)),
            contentDescription = "오전 날씨",
            modifier = Modifier
                .size(24.dp)
                .weight(1f)
        )

        // 오후 아이콘
        Image(
            painter = painterResource(id = getWeatherIconResId(forecast.pmIcon)),
            contentDescription = "오후 날씨",
            modifier = Modifier
                .size(24.dp)
                .weight(1f)
        )

        // 낮 기온 / 밤 기온
        Text(forecast.dayTemp, color = TextHighlight, fontSize = 18.sp, modifier = Modifier.weight(1f))
        Text(forecast.nightTemp, color = TextHighlight, fontSize = 18.sp, modifier = Modifier.weight(1f))

        // 관측 적합도
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("관측 적합도", color = TextHighlight, fontSize = 12.sp)
            Text(forecast.suitability.toPercent(), color = Color(0xFF75FF75), fontSize = 16.sp)
        }
    }
}

@Preview(name = "WeatherDailySection", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun Preview_WeatherDailySection() {
    val previewDaily = listOf(
        DailyForecast("5.27", "100%", "sunny",      "cloud",      "27°", "13°", 85),
        DailyForecast("5.28", "80%",  "cloud",      "rain",       "25°", "12°", 60),
        DailyForecast("5.29", "90%",  "rain",       "rain",       "23°", "11°", 45),
        DailyForecast("5.30", "100%", "rain",       "rain",       "22°", "10°", 20),
        DailyForecast("5.31", "100%", "cloud_sun",  "rain",       "23°", "9°",  40),
        DailyForecast("10.10",  "100%", "rain",       "cloud_moon", "22°", "11°", 35)
    )
    MaterialTheme {
        Surface(color = Color.Black) { DailyForecastListSection(previewDaily) }
    }
}