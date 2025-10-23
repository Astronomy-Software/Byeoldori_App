// ReviewCard.kt
package com.example.byeoldori.ui.components.observatory

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import coil.compose.AsyncImage
import com.example.byeoldori.R
import com.example.byeoldori.domain.Content
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.viewmodel.*
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import com.example.byeoldori.ui.components.community.*

@Composable
fun ReviewSection(
    title: String,
    reviews: List<Review>,
    modifier: Modifier = Modifier,
    onSyncReviewLikeCount: (id: String, next: Int) -> Unit,
    onReviewClick: (Review) -> Unit, //리뷰 객체 넘기기
    onToggleLike: (Review) -> Unit
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
                    ReviewCard(
                        review = review,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReviewClick(review) },
                        onSyncLikeCount = { next -> onSyncReviewLikeCount(review.id, next) }, // 상위로 전파
                        onToggleLike = { onToggleLike(review) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier,
    commentCount: Int = review.commentCount,
    onSyncLikeCount: (Int) -> Unit = {},
    onToggleLike: (() -> Unit)? = null,
) {
    val likeKey = if (':' in review.id) review.id else likedKeyReview(review.id)
    val isLiked = review.liked || (likeKey in LikeState.ids)
    var likeCount by remember { mutableStateOf(review.likeCount) }

    LaunchedEffect(review.likeCount) { likeCount = review.likeCount }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blue800),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .then(modifier)
    ) {
        Column(Modifier.fillMaxSize()) {
            //대표 이미지
            val thumbUrl = review.thumbnail
                ?: (review.contentItems.firstOrNull { it is Content.Image.Url } as? Content.Image.Url)?.url
            val model: Any = thumbUrl ?: R.drawable.img_dummy

            val imageModifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp))

            when (model) {
                is Int -> {
                    Image(
                        painter = painterResource(model),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = imageModifier
                    )
                }
                else -> {
                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = imageModifier
                    )
                }
            }
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
                    review.profile?.let {
                        Icon(
                            painter = painterResource(it),
                            contentDescription = "프로필",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val newCount = if (likeKey in LikeState.ids) likeCount - 1 else likeCount + 1
                            LikeState.ids = if (likeKey in LikeState.ids) LikeState.ids - likeKey else LikeState.ids + likeKey
                            likeCount = newCount
                            onSyncLikeCount(newCount)  //상위 reviews 동기화 → 탭 전환해도 유지
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_thumbs_up),
                            contentDescription = null,
                            tint = if (isLiked) Purple500 else TextHighlight,
                            modifier = Modifier.size(18.dp).clickable { onToggleLike?.invoke() }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("$likeCount", color = TextHighlight, fontSize = 14.sp)
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
                        Text("$commentCount", color = TextHighlight, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Preview(
    name = "ReviewCard – Single",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun Preview_ReviewCard_Single() {
    MaterialTheme {
        Surface(color = Color.Black) {
            ReviewCard(review = dummyReviews.first())
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
                reviews = dummyReviews,
                onSyncReviewLikeCount = { id, next ->
                    val idx = dummyReviews.indexOfFirst { it.id == id }
                    if (idx >= 0) dummyReviews[idx] = dummyReviews[idx].copy(likeCount = next)
                },
                onReviewClick = {},
                onToggleLike = {}
            )
        }
    }
}