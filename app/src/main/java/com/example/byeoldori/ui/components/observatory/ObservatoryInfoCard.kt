package com.example.byeoldori.ui.components.observatory

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.domain.Observatory.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.dummyReviews

private const val TAG_CARD = "ObservatoryCard"

//관측적합도 색상 구분용
fun suitabilityColor(score: Int): Color = when (score) {
    in 0..33 -> ErrorRed
    in 34..66 -> WarningYellow
    else -> SuccessGreen
}

@Composable
fun ObservatoryInfoCard(
    modifier: Modifier = Modifier,
    info: MarkerInfo,
    listState: LazyListState,
    currentLat: Double? = null,
    currentLon: Double? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Background {  }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 15.dp)
                ,
            ) {
                // 1) 관측지 기본 정보
                item {
                    BasicInfoCard(info = info)
                    Spacer(Modifier.height(12.dp))
                }

                // 2) 현재 날씨 카드
                item {
                    val lat = currentLat
                    val lon = currentLon
                    if (lat != null && lon != null) {
                        Log.d(TAG_CARD, "CurrentWeatherSection(lat=$lat, lon=$lon)")
                        CurrentWeatherSection(lat = lat, lon = lon)
                    } else {
                        Log.d(TAG_CARD, "No lat/lon -> show DUMMY")
                        // 더미 표시
                        WeatherInfoCard(currentWeather = CurrentWeather("14°","35%","→ 3 m/s",75,75))
                    }
                    Spacer(Modifier.height(16.dp))
                }
                //2-1) 시간별 섹션 (더미 주입)
                item {
                    WeatherHourlyPanel(
                        lat = info.latitude,
                        lon = info.longitude
                    )
                    Spacer(Modifier.height(12.dp))
                }

                //2-2) 일별 섹션 (더미 주입)
                item {
                    WeatherDailyPanel(
                        lat = info.latitude,
                        lon = info.longitude
                    )
                    Spacer(Modifier.height(16.dp))
                }

                //3) 관측 리뷰 섹션
                item { // TODO : 이런 데이터들은 컴포넌트 내부에서 작성하지말고 변수같은느낌으로 만들어서 하기
                    ReviewSection(
                        title = "해당 관측지에서 진행한 관측후기",
                        reviews = dummyReviews,
                        onSyncReviewLikeCount = { id, next ->
                            val i = dummyReviews.indexOfFirst { it.id == id }
                            if (i >= 0) dummyReviews[i] = dummyReviews[i].copy(likeCount = next)
                        }
                    )
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}



// TODO : BasicInfoCard는 따로 컴포넌트로 나눠야합니다
@Preview(
    name = "ObservatoryInfoCard",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,   // 픽셀 6 레벨(권장): 411~412
    heightDp = 1900  // 내용을 넉넉히 보기 위해 크게
)
@Composable
private fun Preview_ObservatoryInfoCard() {
    val dummyMarkerInfo = MarkerInfo(
        name = "오산천",
        type = ObservatoryType.POPULAR,
        reviewCount = 103,
        likeCount = 57,
        rating = 4.3f,
        suitability = 87,
        address = "경기도 오산시 오산천로 254-5",
        drawableRes = R.drawable.img_dummy,
        latitude = 37.33,
        longitude = 126.23
    )

    MaterialTheme {
        Surface(color = Color.Black) { // 배경을 실제 앱과 유사하게 설정
            Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                ObservatoryInfoCard(
                    info = dummyMarkerInfo,
                    listState = rememberLazyListState(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}