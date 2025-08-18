// ObservatoryInfoCard.kt
package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.R

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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF473A9D))
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
                        Review("1", "페르세우스 유성우 관측한 날~", "아이마카", 5.0f, 21, 21, R.drawable.star_image),
                        Review("2", "토성 고리 봄", "아이마카", 5.0f, 20, 5, R.drawable.star_image),
                        Review("3", "목성 위성 본 날", "아이마카", 5.0f, 40, 8, R.drawable.star_image),
                        Review("4", "태양 흑점 본 날", "아이마카", 5.0f, 30, 10, R.drawable.star_image),
                        Review("5", "태양 흑점 본 날", "아이마카", 5.0f, 30, 10, R.drawable.star_image)
                    )
                )
            }
        }
    }
}

//기본 정보 카드
@Composable
private fun BasicInfoCard(info: MarkerInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF241860)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 20.dp)
                ) {
                    Spacer(Modifier.height(8.dp))
                    Text(info.name, fontSize = 30.sp, color = Color.White)
                    Spacer(Modifier.height(15.dp))

                    Row(Modifier.fillMaxWidth()) {
                        Text("리뷰", color = Color.White, modifier = Modifier.weight(1.2f))
                        Text(
                            "${info.reviewCount}",
                            color = Color.White,
                            modifier = Modifier.weight(0.5f).padding(start = 10.dp)
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("좋아요", color = Color.White, modifier = Modifier.weight(1.2f))
                        Text(
                            "${info.likeCount}",
                            color = Color.White,
                            modifier = Modifier.weight(0.5f).padding(start = 10.dp)
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("평점", color = Color.White, modifier = Modifier.weight(1.2f))
                        Text(
                            "${info.rating}",
                            color = Color.White,
                            modifier = Modifier.weight(0.5f).padding(start = 10.dp)
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("현재 관측 적합도", color = Color.White, modifier = Modifier.weight(1.2f))
                        Text(
                            "${info.suitability}",
                            color = Color.Green,
                            modifier = Modifier.weight(0.5f).padding(start = 10.dp)
                        )
                    }
                }

                Image(
                    painter = painterResource(id = info.drawableRes),
                    contentDescription = null,
                    modifier = Modifier
                        .width(180.dp)
                        .height(160.dp)
                        .padding(top = 60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                Text("도로명 주소", color = Color.White, modifier = Modifier.weight(2f), maxLines = 1)
                Text(
                    text = info.address,
                    color = Color.White,
                    modifier = Modifier.weight(5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
