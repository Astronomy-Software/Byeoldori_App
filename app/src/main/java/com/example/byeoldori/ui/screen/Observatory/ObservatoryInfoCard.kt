package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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


@Composable
fun ObservatoryInfoCard(info: MarkerInfo, scrollState: ScrollState, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            //.height(250.dp)
            .padding(0.dp),
        shape = RoundedCornerShape(size = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1F75))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .verticalScroll(scrollState)
        ) {
            //박스로 기본 정보 영역 감싸기
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF241860)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
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
                                Text("${info.reviewCount}", color = Color.White,
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .padding(start = 10.dp))
                            }

                            Row(Modifier.fillMaxWidth()) {
                                Text("좋아요", color = Color.White, modifier = Modifier.weight(1.2f))
                                Text("${info.likeCount}", color = Color.White,
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .padding(start = 10.dp))
                            }

                            Row(Modifier.fillMaxWidth()) {
                                Text("평점", color = Color.White, modifier = Modifier.weight(1.2f))
                                Text("${info.rating}", color = Color.White,
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .padding(start = 10.dp))
                            }

                            Row(Modifier.fillMaxWidth()) {
                                Text("현재 관측 적합도",
                                    color = Color.White,
                                    modifier = Modifier.weight(1.2f))
                                Text("${info.suitability}", color = Color.Green,
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .padding(start = 10.dp))
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
                                .border(
                                    width = 2.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp)
                    ) {
                        Text(
                            "도로명 주소",
                            color = Color.White,
                            modifier = Modifier.weight(2f),
                            maxLines = 1
                        )
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
            Spacer(modifier = Modifier.height(12.dp))

            //날씨 카드 삽입
            WeatherInfoCard(
                temperature = "14°",
                humidity = "35%",
                windSpeed = "→  3m/s",
                suitability = "75%"
            )
            //아래에 더미 공간 추가
            Spacer(modifier = Modifier.height(400.dp))
        }
    }
}
