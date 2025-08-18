package com.example.byeoldori.ui.screen.Observatory
import com.example.byeoldori.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherInfoCard(
    temperature: String,
    humidity: String,
    windSpeed: String,
    suitability: String,
    modifier: Modifier = Modifier
) {
    Text("해당 위치의 현재 날씨", color = Color.White, fontSize = 14.sp)
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(12.dp))

        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherItem(
                    title = "기온",
                    value = temperature,
                    iconResId = R.drawable.temp1,
                    modifier = Modifier.weight(1f))
                WeatherItem(
                    title = "습도",
                    value = humidity,
                    iconResId = R.drawable.water11,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherItem(
                    title = "바람",
                    value = windSpeed,
                    iconResId = R.drawable.wind1,
                    modifier = Modifier.weight(1f)
                )
                WeatherItem(
                    title = "관측 적합도",
                    value = suitability,
                    iconResId = R.drawable.thumbs_up1,
                    valueColor = Color(0xFF6EFFA6),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
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



@Composable
fun WeatherForecastScrollSection(forecasts: List<HourlyForecast>) {
    val grouped = forecasts.groupBy { it.date }.toList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF241860), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
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
                        Text(date, color = Color.White, fontSize = 20.sp)
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
                                    .background(Color.White.copy(alpha = 0.8f))
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
        Text(forecast.time, color = Color.White, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(5.dp))
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Text(forecast.temperature, color = Color.White, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.water22),
                contentDescription = "습도",
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(forecast.precipitation, color = Color.White, fontSize = 12.sp)
        }

        Text("관측 적합도", color = Color.White, fontSize = 12.sp)
        Text(forecast.suitability, color = Color(0xFF75FF75), fontSize = 18.sp)
    }
}


@Composable
fun WeatherItem(
    title: String,
    value: String,
    iconResId: Int,
    valueColor: Color = Color.White,
    modifier: Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .defaultMinSize(minWidth = 170.dp, minHeight = 100.dp)
            .background(Color(0xFF241860), RoundedCornerShape(15.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$title 아이콘",
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, color = Color.White, fontSize = 22.sp)
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
        Text(forecast.date, color = Color.White, fontSize = 18.sp, modifier = Modifier.weight(1f))

        // 강수 확률
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
            Image(painter = painterResource(id = R.drawable.water22), contentDescription = "강수",modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(forecast.precipitation, color = Color.White, fontSize = 12.sp)
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
        Text(forecast.dayTemp, color = Color.White, fontSize = 18.sp, modifier = Modifier.weight(1f))
        Text(forecast.nightTemp, color = Color.White, fontSize = 18.sp, modifier = Modifier.weight(1f))

        // 관측 적합도
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("관측 적합도", color = Color.White, fontSize = 12.sp)
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
            .background(Color(0xFF241860), RoundedCornerShape(15.dp))
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




