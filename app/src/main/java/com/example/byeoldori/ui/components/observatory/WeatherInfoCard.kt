package com.example.byeoldori.ui.components.observatory
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Observatory.CurrentWeather
import com.example.byeoldori.viewmodel.Observatory.WeatherViewModel

private const val TAG_UIWX = "CurrentWeatherUI"

@Composable
fun CurrentWeatherSection(lat: Double, lon: Double, vm: WeatherViewModel = hiltViewModel()) {
    val current by vm.current.collectAsState(null)
    val loading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(lat, lon) {
        Log.d(TAG_UIWX, "LaunchedEffect -> vm.getCurrent(lat=$lat, lon=$lon)")
        vm.getCurrent(lat, lon)
    }

    when {
        current != null -> {
            Log.d(TAG_UIWX, "render current = $current")
            WeatherInfoCard(current!!)
        }
        loading -> {
            Log.d(TAG_UIWX, "loading...")
            CircularProgressIndicator()
        }
        error != null -> {
            Log.e(TAG_UIWX, "error: $error")
            Text("현재 날씨를 불러오지 못했습니다.", color = Color.Gray)
        }
    }
}


// TODO : 여기도 내부의 컴포넌트들 파일 나눠야합니다.
@Composable
fun WeatherInfoCard(
    currentWeather: CurrentWeather,
    modifier: Modifier = Modifier
) {
    Text("해당 위치의 현재 날씨", color = TextHighlight, fontSize = 14.sp)
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(20.dp))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherItem(
                    title = "기온",
                    value = currentWeather.temperature,
                    iconResId = R.drawable.ic_temperature,
                    modifier = Modifier.weight(1f))
                WeatherItem(
                    title = "습도",
                    value = currentWeather.humidity,
                    iconResId = R.drawable.ic_pop,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherItem(
                    title = "바람",
                    value = currentWeather.windSpeed,
                    iconResId = R.drawable.ic_wind,
                    modifier = Modifier.weight(1f),
                    windDirection = currentWeather.windDirection
                )
                WeatherItem(
                    title = "관측 적합도",
                    value = currentWeather.suitability,
                    iconResId = R.drawable.ic_thumbs_up,
                    valueColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WeatherItem(
    title: String,
    value: String,
    iconResId: Int,
    valueColor: Color = TextHighlight,
    modifier: Modifier = Modifier,
    windDirection: Int? = null
) {
    Column(
        modifier = modifier
            .defaultMinSize(minWidth = 170.dp, minHeight = 100.dp)
            .background(Blue800, RoundedCornerShape(15.dp))
            .padding(12.dp)
    ) {
        // 제목
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

        if (windDirection != null) {
            // 풍향 각도 계산 (16방위 스냅)
            val norm = ((windDirection % 360) + 360) % 360
            val step = ((norm + 11.25) / 22.5).toInt()
            val drawAngle = (step * 22.5f) % 360f

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = "풍향",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = (drawAngle + 180f) % 360f }
                )
                Spacer(Modifier.width(6.dp))
                Text(value, color = valueColor, fontSize = 20.sp)
            }
        } else {
            Text(value, color = valueColor, fontSize = 20.sp)
        }
    }
}


@Preview(
    name = "WeatherInfoCard – Summary",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_WeatherInfoCard_Summary() {
    val previewWeather = CurrentWeather(
        temperature = "14°",
        humidity = "35%",
        windSpeed = "3 m/s",
        suitability = "75%",
        windDirection = 245
    )

    MaterialTheme {
        Surface(color = Color.Black) {
            WeatherInfoCard(currentWeather = previewWeather)
        }
    }
}