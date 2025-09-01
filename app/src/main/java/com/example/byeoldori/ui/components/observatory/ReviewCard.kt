// ReviewCard.kt
package com.example.byeoldori.ui.components.observatory

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Observatory.Review
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch


// TODO : 리뷰섹션 좌우 드래그able하게 변경
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReviewSection(
    title: String,
    reviews: List<Review>,
    modifier: Modifier = Modifier
) {
    // ----- 페이징 상태 -----
    val pageSize = 4
    val pageCount = if (reviews.isEmpty()) 1 else ((reviews.size - 1) / pageSize + 1)
    val pagerState = rememberPagerState(initialPage = 0) // 페이지 상태를 저장
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        // 타이틀 + 페이지 컨트롤
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TextHighlight,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                //페이지를 부드럽게 넘기기 위해 animateScrollToPage 사용
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                enabled = pagerState.currentPage > 0
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_before),
                    contentDescription = "이전",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text("${pagerState.currentPage + 1} / $pageCount", color = TextHighlight)
            IconButton(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                enabled = pagerState.currentPage < pageCount - 1
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_next),
                    contentDescription = "다음",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        HorizontalPager(
            count = pageCount,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(540.dp)
        ) { page ->
            val start = page * pageSize
            val pageItems = reviews.drop(start).take(pageSize)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2), //2개의 카드를 나란히
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(pageItems, key = { idx, item -> "${item.id}#$start+$idx" }) { _, review ->
                    ReviewCard(review)
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blue800),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(review.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Column(Modifier.padding(start = 15.dp)) {
                Text(
                    text = review.title,
                    color = TextHighlight,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.profile1),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(review.author, color = TextHighlight, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text("별점 : ${review.rating}", color = TextHighlight, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_thumbs_up),
                            contentDescription = null,
                            tint = TextHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${review.likeCount}", color = TextHighlight, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(x=(-8).dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_comment),
                            contentDescription = null,
                            tint = TextHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${review.commentCount}", color = TextHighlight, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

private val previewReviews = listOf(
    Review("1", "페르세우스 유성우 관측한 날~", "아이마카", 5.0f, 21, 21, R.drawable.img_dummy),
    Review("2", "토성 고리 봄",              "아이마카", 4.8f, 20,  5, R.drawable.img_dummy),
    Review("3", "목성 위성 본 날",           "아이마카", 4.7f, 40,  8, R.drawable.img_dummy),
    Review("4", "태양 흑점 본 날",           "아이마카", 4.9f, 30, 10, R.drawable.img_dummy),
    // 필요시 더 추가
)

@Preview(
    name = "ReviewCard – Single",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_ReviewCard_Single() {
    MaterialTheme {
        Surface(color = Color.Black) {
            ReviewCard(review = previewReviews.first())
        }
    }
}

@Preview(
    name = "ReviewSection – 2x2 Grid (page 1)",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_ReviewSection_Grid() {
    MaterialTheme {
        Surface(color = Color.Black) {
            ReviewSection(
                title = "해당 관측지에서 진행한 관측후기",
                reviews = previewReviews
            )
        }
    }
}
