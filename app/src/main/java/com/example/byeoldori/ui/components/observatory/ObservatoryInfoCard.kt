// ObservatoryInfoCard.kt
package com.example.byeoldori.ui.components.observatory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.Blue800
import com.example.byeoldori.ui.theme.Purple700
import com.example.byeoldori.ui.theme.Purple800
import com.example.byeoldori.viewmodel.Observatory.ObservatoryType
import com.example.byeoldori.ui.theme.SuccessGreen
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.viewmodel.Observatory.MarkerInfo
import com.example.byeoldori.viewmodel.Observatory.Review

@Composable
fun ObservatoryInfoCard(
    info: MarkerInfo,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        //그라데이션 배경
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Blue800, Purple700)
                    ),
                )
                .fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // 1) 관측지 기본 정보
                item {
                    BasicInfoCard(info)
                    Spacer(Modifier.height(12.dp))
                }

                // 2) 날씨 카드
                item {
                    WeatherInfoCard(
                        temperature = "14°",
                        humidity = "35%",
                        windSpeed = "→  3m/s",
                        suitability = "75%"
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // 3) 관측 리뷰 섹션
                item {
                    ReviewSection(
                        title = "해당 관측지에서 진행한 관측후기",
                        reviews = listOf(
                            Review(
                                "1",
                                "페르세우스 유성우 관측한 날~",
                                "아이마카",
                                5.0f,
                                21,
                                21,
                                R.drawable.img_dummy
                            ),
                            Review("2", "토성 고리 봄", "아이마카", 5.0f, 20, 5, R.drawable.img_dummy),
                            Review("3", "목성 위성 본 날", "아이마카", 5.0f, 40, 8, R.drawable.img_dummy),
                            Review("4", "태양 흑점 본 날", "아이마카", 5.0f, 30, 10, R.drawable.img_dummy),
                            Review("5", "태양 흑점 본 날", "아이마카", 5.0f, 30, 10, R.drawable.img_dummy)
                        )
                    )
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

//기본 정보 카드
@Composable
private fun BasicInfoCard(info: MarkerInfo) {
    Text(info.name, fontSize = 30.sp, color = TextHighlight)
    Spacer(Modifier.height(15.dp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Blue800),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 20.dp)
                ) {
                    //Spacer(Modifier.height(8.dp))
                    //Text(info.name, fontSize = 30.sp, color = TextHighlight)
                    //Spacer(Modifier.height(15.dp))

                    Row(Modifier.fillMaxWidth()) {
                        Text("리뷰", color = TextHighlight, modifier = Modifier.weight(1.2f))
                        Text(
                            "${info.reviewCount}",
                            color = TextHighlight,
                            modifier = Modifier.weight(0.5f).padding(start = 10.dp)
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("좋아요", color = TextHighlight, modifier = Modifier.weight(1.2f))
                        Text(
                            "${info.likeCount}",
                            color = TextHighlight,
                            modifier = Modifier.weight(0.5f).padding(start = 10.dp)
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("평점", color = TextHighlight, modifier = Modifier.weight(1.2f))
                        Text(
                            "${info.rating}",
                            color = TextHighlight,
                            modifier = Modifier.weight(0.5f).padding(start = 10.dp)
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("현재 관측 적합도", color = TextHighlight, modifier = Modifier.weight(1.2f))
                        Text(
                            "${info.suitability}",
                            color = SuccessGreen,
                            modifier = Modifier.weight(0.5f).padding(start = 10.dp)
                        )
                    }
                }

                Image(
                    painter = painterResource(id = info.drawableRes),
                    contentDescription = null,
                    modifier = Modifier
                        .width(140.dp)
                        .height(100.dp)
                        .padding(top = 0.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, TextHighlight, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text("도로명 주소", color = TextHighlight, modifier = Modifier.weight(2f), maxLines = 1)
                Text(
                    text = info.address,
                    color = TextHighlight,
                    modifier = Modifier.weight(5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

//preview용 더미 데이터
private val previewMarkerInfo = MarkerInfo(
    name = "오산천",
    type = ObservatoryType.POPULAR,
    reviewCount = 103,
    likeCount = 57,
    rating = 4.3f,
    suitability = 87,
    address = "경기도 오산시 오산천로 254-5",
    drawableRes = R.drawable.img_dummy // 프로젝트 내 임의 이미지
)

@Preview(
    name = "BasicInfoCard",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_BasicInfoCard_Only() {
    MaterialTheme {
        Surface(color = Color.Black) { // 앱 실행과 유사하게 Surface로 감싸기
            BasicInfoCard(info = previewMarkerInfo)
        }
    }
}
