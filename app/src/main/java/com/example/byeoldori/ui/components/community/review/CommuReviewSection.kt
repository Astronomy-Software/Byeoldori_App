package com.example.byeoldori.ui.components.community.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.community.EditorItem
import com.example.byeoldori.ui.components.community.SortBar
import com.example.byeoldori.ui.components.observatory.ReviewCard
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Observatory.Review
import com.example.byeoldori.viewmodel.dummyReviewComments
import com.example.byeoldori.viewmodel.dummyReviews

// 정렬 타입
enum class ReviewSort(val label: String) {
    Latest("최신순"), Like("좋아요순"), View("조회수순")
}

// Review 탭 콘텐츠
@Composable
fun CommuReviewSection(
    reviewsAll: List<Review>,
    onWriteClick: () -> Unit = {},
    onReviewClick: (Review) -> Unit,
    onSyncReviewLikeCount: (id: String, next: Int) -> Unit
) {
    var searchText by remember { mutableStateOf("") } //초기값이 빈 문자열인 변할 수 있는 상태 객체
    var sort by remember { mutableStateOf(ReviewSort.Latest) }
    val gridState = rememberLazyGridState()

    // 검색 + 정렬 적용
    val filtered by remember(searchText, sort) {
        derivedStateOf {
            val q = searchText.trim()
            val base = if (q.isEmpty()) reviewsAll else {
                reviewsAll.filter { r ->
                    val matchesTitle   = r.title.contains(q, ignoreCase = true)
                    val matchesAuthor  = r.author.contains(q, ignoreCase = true)
                    val matchesContent = r.contentItems
                        .filterIsInstance<EditorItem.Paragraph>()
                        .any { it.value.text.contains(q, ignoreCase = true) }
                    matchesTitle || matchesAuthor || matchesContent
                }
            }
            when (sort) {
                ReviewSort.Latest -> base.sortedByDescending { it.createdAt }
                ReviewSort.Like   -> base.sortedByDescending { it.likeCount }
                ReviewSort.View   -> base.sortedByDescending { it.viewCount }
            }
        }
    }

    //정렬 기준이 바뀔 때 스크롤 맨 위로 이동
    LaunchedEffect(sort) {
        gridState.scrollToItem(0)
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            com.example.byeoldori.ui.components.community.SearchBar(
                searchQuery = searchText,
                onSearch = { searchText = it }
            )
            Spacer(Modifier.height(10.dp))
            // 정렬 칩
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 정렬 바
                SortBar(
                    current = sort,
                    options = ReviewSort.entries.toList(),
                    label = { it.label },
                    onSelect = { sort = it }
                )
            }
            Spacer(Modifier.height(12.dp))
            // 2열 그리드
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2), //컬럼 개수 2개
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = filtered,
                    key = { it.id }
                ) { review ->
                    val liveCommentCount = dummyReviewComments.count { it.reviewId == review.id }
                    val liveLikeCount = review.likeCount
                    ReviewCard(
                        review = review.copy(
                            commentCount = liveCommentCount,
                        ),
                        modifier = Modifier.clickable { onReviewClick(review) },
                        onSyncLikeCount = { next ->            // ★ NEW: 카드에서 좋아요 바꾸면
                            onSyncReviewLikeCount(review.id, next)  // 상위 reviews 동기화
                        }
                    )
                }
                item { Spacer(Modifier.height(60.dp)) }
            }
        }
        //리뷰 작성 버튼
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = -(24.dp), y = -(10.dp))
                .size(56.dp)
                .clickable { onWriteClick() }
                .background(Blue800, shape = CircleShape)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_notebook_pen),
                contentDescription = "글쓰기",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
private fun Preview_ScreenWithWriteButton() {
    MaterialTheme {
        CommuReviewSection(
            reviewsAll = dummyReviews,
            onWriteClick = {  },
            onReviewClick = { review ->
                println("리뷰 클릭됨: ${review.title}")
            },
            onSyncReviewLikeCount = { id, next ->
                val idx = dummyReviews.indexOfFirst { it.id == id }
                if (idx >= 0) dummyReviews[idx] = dummyReviews[idx].copy(likeCount = next)
            }
        )
    }
}