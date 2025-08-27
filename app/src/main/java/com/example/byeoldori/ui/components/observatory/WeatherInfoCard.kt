package com.example.byeoldori.ui.components.observatory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.Blue800
import com.example.byeoldori.ui.theme.SuccessGreen
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.viewmodel.Observatory.DailyForecast
import com.example.byeoldori.viewmodel.Observatory.HourlyForecast

@Composable
fun WeatherInfoCard(
    temperature: String,
    humidity: String,
    windSpeed: String,
    suitability: String,
    modifier: Modifier = Modifier,
    showForecasts: Boolean = true
) {
    Text("해당 위치의 현재 날씨", color = TextHighlight, fontSize = 14.sp)
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(20.dp))

        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherItem(
                    title = "기온",
                    value = temperature,
                    iconResId = R.drawable.ic_temperature,
                    modifier = Modifier.weight(1f))
                WeatherItem(
                    title = "습도",
                    value = humidity,
                    iconResId = R.drawable.ic_pop,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherItem(
                    title = "바람",
                    value = windSpeed,
                    iconResId = R.drawable.ic_wind,
                    modifier = Modifier.weight(1f)
                )
                WeatherItem(
                    title = "관측 적합도",
                    value = suitability,
                    iconResId = R.drawable.ic_thumbs_up,
                    valueColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    if(showForecasts) {
        Spacer(modifier = Modifier.height(16.dp))
        val dummyForecasts = listOf(
            HourlyForecast("5.23", "4시", "15°", "cloud_sun", "60%", "85%"),
            HourlyForecast("5.23", "5시", "16°", "sunny", "55%", "82%"),
            HourlyForecast("5.23", "6시", "17°", "rain", "70%", "60%"),
            HourlyForecast("5.24", "1시", "13°", "cloud_moon", "80%", "90%"),
            HourlyForecast("5.24", "2시", "12°", "cloud_sun", "85%", "88%"),
            HourlyForecast("5.24", "3시", "14°", "sunny", "60%", "60%"),
            HourlyForecast("5.25", "9시", "20°", "cloud_sun", "60%", "60%"),
            HourlyForecast("5.25", "10시", "22°", "sunny", "40%", "80%"),
            HourlyForecast("5.25", "11시", "23°", "sunny", "30%", "80%"),

            )
        WeatherForecastScrollSection(forecasts = dummyForecasts)

        val sampleDailyForecasts = listOf(
            DailyForecast("5.27", "100%", "sunny", "cloud", "27°", "13°", "85%"),
            DailyForecast("5.28", "80%", "cloud", "rain", "25°", "12°", "60%"),
            DailyForecast("5.29", "90%", "rain", "rain", "23°", "11°", "45%"),
            DailyForecast("5.30", "100%", "rain", "rain", "22°", "10°", "20%"),
            DailyForecast("5.31", "100%", "cloud_sun", "rain", "23°", "9°", "40%"),
            DailyForecast("6.1", "100%", "rain", "cloud_moon", "22°", "11°", "35%"),

            )
        DailyForecastListSection(forecasts = sampleDailyForecasts)
    }
}



@Composable
fun WeatherForecastScrollSection(forecasts: List<HourlyForecast>) {
    val grouped = forecasts.groupBy { it.date }.toList() // 날짜별로 그룹화

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
        Text(forecast.suitability, color = Color(0xFF75FF75), fontSize = 18.sp)
    }
}


@Composable
fun WeatherItem(
    title: String,
    value: String,
    iconResId: Int,
    valueColor: Color = TextHighlight,
    modifier: Modifier
) {
    Column(
        modifier = Modifier
            //.fillMaxHeight()
            .defaultMinSize(minWidth = 170.dp, minHeight = 100.dp)
            .background(Blue800, RoundedCornerShape(15.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$title 아이콘",
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, color = TextHighlight, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(value, color = valueColor, fontSize = 20.sp)
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
       // Spacer(modifier = Modifier.height(15.dp))
        Text(forecast.date, color = TextHighlight, fontSize = 18.sp, modifier = Modifier.weight(1f))

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
            Text(forecast.suitability, color = Color(0xFF75FF75), fontSize = 16.sp)
        }
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

private val previewHourly = listOf(
    HourlyForecast("5.23", "4시", "15°", "cloud_sun", "60%", "85%"),
    HourlyForecast("5.23", "5시", "16°", "sunny",     "55%", "82%"),
    HourlyForecast("5.23", "6시", "17°", "rain",      "70%", "60%"),
    HourlyForecast("5.24", "1시", "13°", "cloud_moon","80%", "90%"),
    HourlyForecast("5.24", "2시", "12°", "cloud_sun", "85%", "88%"),
    HourlyForecast("5.24", "3시", "14°", "sunny",     "60%", "60%")
)

private val previewDaily = listOf(
    DailyForecast("5.27", "100%", "sunny",      "cloud",      "27°", "13°", "85%"),
    DailyForecast("5.28", "80%",  "cloud",      "rain",       "25°", "12°", "60%"),
    DailyForecast("5.29", "90%",  "rain",       "rain",       "23°", "11°", "45%"),
    DailyForecast("5.30", "100%", "rain",       "rain",       "22°", "10°", "20%"),
    DailyForecast("5.31", "100%", "cloud_sun",  "rain",       "23°", "9°",  "40%"),
    DailyForecast("6.1",  "100%", "rain",       "cloud_moon", "22°", "11°", "35%")
)



@Preview(
    name = "WeatherInfoCard – Summary",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_WeatherInfoCard_Summary() {
    MaterialTheme {
        Surface(color = Color.Black) {
            WeatherInfoCard(
                temperature = "14°",
                humidity = "35%",
                windSpeed = "→ 3 m/s",
                suitability = "75%",
                showForecasts = false   // ✅ 예보 섹션 숨기기
            )
        }
    }
}

@Preview(
    name = "Hourly – Scroll Section",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_WeatherForecastScrollSection() {
    MaterialTheme {
        Surface(color = Color.Black) {
            WeatherForecastScrollSection(forecasts = previewHourly)
        }
    }
}

@Preview(
    name = "Daily – List Section",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_DailyForecastListSection() {
    MaterialTheme {
        Surface(color = Color.Black) {
            DailyForecastListSection(forecasts = previewDaily)
        }
    }
}


