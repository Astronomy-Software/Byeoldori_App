package com.example.byeoldori.ui.components.observatory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Observatory.MarkerInfo
import com.example.byeoldori.domain.Observatory.ObservatoryType

//기본 정보 카드
@Composable
fun BasicInfoCard(
    info: MarkerInfo,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(info.name, fontSize = 30.sp, color = TextHighlight)
        Spacer(Modifier.height(15.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = Blue800),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 20.dp)
                    ) {
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
                            Text(
                                "현재 관측 적합도",
                                color = TextHighlight,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                "${info.suitability}",
                                color = suitabilityColor(info.suitability),
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
                    Text(
                        "도로명 주소",
                        color = TextHighlight,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = info.address,
                        color = TextHighlight,
                        modifier = Modifier.weight(5f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private val previewMarkerInfo = MarkerInfo(
    name = "오산천",
    type = ObservatoryType.POPULAR,
    reviewCount = 103,
    likeCount = 57,
    rating = 4.3f,
    suitability = 55,
    address = "경기도 오산시 오산천로 254-5",
    drawableRes = R.drawable.img_dummy,
    latitude = 36.23,
    longitude = 127.23
)

@Preview(
    name = "BasicInfoCard",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_BasicInfoCard() {
    Surface(color = Color.Black) {
        BasicInfoCard(info = previewMarkerInfo)
    }
}