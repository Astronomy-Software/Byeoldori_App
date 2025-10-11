package com.example.byeoldori.ui.components.observatory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Observatory.HourlyForecast
import com.example.byeoldori.viewmodel.Observatory.WeatherViewModel
import com.example.byeoldori.viewmodel.UiState
import java.time.format.DateTimeFormatter


@Composable
fun WeatherHourlyPanel(
    lat: Double,
    lon: Double,
    viewModel: WeatherViewModel = hiltViewModel() //Hilt가 WeatherViewModel 객체를 만들어서 자동으로 넣어줌
) {
    val hourlyState by viewModel.hourly.collectAsState() //Compose State로 변환

    LaunchedEffect(lat,lon) {
        viewModel.getHourly(lat,lon)
    }

    when(val state = hourlyState) {
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
                WeatherHourlySection(forecasts = state.data)
            }
        }
        is UiState.Error -> Text("날씨 정보를 불러오지 못했습니다: ${state.message}")
    }
}

@Composable
fun WeatherHourlySection(forecasts: List<HourlyForecast>) {
    val grouped = forecasts.groupBy { it.date }.toList() // 날짜별로 그룹화
    val dateFmt = remember { DateTimeFormatter.ofPattern("M.d") }
    val timeFmt = remember { DateTimeFormatter.ofPattern("H시") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue800, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()) //수평 스크롤
                .padding(vertical = 8.dp)
        ) {
            grouped.forEachIndexed { index, (date, hourlyList) ->
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    // 날짜 + 시간별 Forecast 박스
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .padding(end = 12.dp)
                    ) {
                        Text(date, color = TextHighlight, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            hourlyList.forEach { forecast ->
                                ForecastItem(forecast)
                            }
                        }
                    }

                    // 구분선
                    if (index < grouped.size - 1) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .fillMaxHeight()
                                    .background(TextHighlight.copy(alpha = 0.8f))
                                    .align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastItem(forecast: HourlyForecast) {
    val iconRes = getWeatherIconResId(forecast.iconName)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .offset(x=-(5).dp)
    ) {
        Text(forecast.time, color = TextHighlight, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(5.dp))
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Text(forecast.temperature, color = TextHighlight, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_humidity),
                contentDescription = "습도",
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(forecast.precipitation, color = TextHighlight, fontSize = 12.sp)
        }

        Text("관측 적합도", color = TextHighlight, fontSize = 12.sp)
        Text(forecast.suitability.toPercent(),  color = suitabilityColor(forecast.suitability), fontSize = 18.sp)
    }
}

@Preview(name = "WeatherHourlySection", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun Preview_WeatherHourlySection() {
    val previewHourly = listOf(
        HourlyForecast("5.23", "4시", "15°", "cloud_sun", "60%", 85),
        HourlyForecast("5.23", "5시", "16°", "sunny",     "55%", 82),
        HourlyForecast("5.23", "6시", "17°", "rain",      "70%", 12),
        HourlyForecast("5.24", "1시", "13°", "cloud_moon","80%", 50),
        HourlyForecast("5.24", "2시", "12°", "cloud_sun", "85%", 88),
        HourlyForecast("5.24", "3시", "14°", "sunny",     "60%", 60)
    )
    MaterialTheme {
        Surface(color = Color.Black) { WeatherHourlySection(previewHourly) }
    }
}