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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.data.model.dto.ReviewResponse
import com.example.byeoldori.data.model.dto.SortBy
import com.example.byeoldori.domain.Content
import com.example.byeoldori.ui.components.community.EditorItem
import com.example.byeoldori.ui.components.community.SortBar
import com.example.byeoldori.ui.components.observatory.ReviewCard
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.components.community.freeboard.FreeBoardSort
import com.example.byeoldori.ui.components.community.freeboard.formatCreatedAt
import com.example.byeoldori.viewmodel.ReviewViewModel
import com.example.byeoldori.viewmodel.UiState
import com.example.byeoldori.viewmodel.dummyReviewComments
import com.example.byeoldori.viewmodel.dummyReviews

// 정렬 타입
enum class ReviewSort(val label: String) {
    Latest("최신순"), Like("좋아요순"), View("조회수순")
}

fun ReviewResponse.toDomain(): Review = Review(
    id = id.toString(),
    title = title,
    author = authorNickname ?: "익명",
    profile = null, // 서버에 프로필 리소스 없으면 null 유지
    createdAt = formatCreatedAt(createdAt),
    viewCount = viewCount,
    likeCount = likeCount,
    commentCount = commentCount,
    // 목록에서는 요약만 있으므로 가볍게 파라그래프 하나로 구성
    contentItems = listOf(Content.Text(contentSummary.orEmpty())),
    // 아래 필드는 상세 조회 시 채워넣을 수 있음
    target = "",
    site = "",
    equipment = "",
    date = "",
    startTime = "",
    endTime = "",
    rating = 0,
    siteScore = 0
)


// Review 탭 콘텐츠
@Composable
fun CommuReviewSection(
    vm: ReviewViewModel = hiltViewModel(),
    reviewsAll: List<Review>,
    onWriteClick: () -> Unit = {},
    onReviewClick: (Review) -> Unit,
    onChangeSort: (SortBy) -> Unit = {},
    onSyncReviewLikeCount: (id: String, next: Int) -> Unit
) {
    var searchText by remember { mutableStateOf("") } //초기값이 빈 문자열인 변할 수 있는 상태 객체
    var sort by remember { mutableStateOf(ReviewSort.Latest) }
    val gridState = rememberLazyGridState()

    val state by vm.postsState.collectAsState()
    val likeCounts by vm.likeCounts.collectAsState()

    val reviewsAll by remember(state) {
        mutableStateOf(
            when (state) {
                is UiState.Success -> (state as UiState.Success<List<ReviewResponse>>).data.map { it.toDomain() }
                else -> emptyList()
            }
        )
    }

    // 검색만
    val filtered = run {
        val q = searchText.trim()
        if (q.isEmpty()) reviewsAll else {
            val k = q.lowercase()
            reviewsAll.filter { r ->
                r.title.lowercase().contains(k) ||
                r.author.lowercase().contains(k) ||
                r.contentItems.filterIsInstance<EditorItem.Paragraph>().any { it.value.text.lowercase().contains(k) }
            }
        }
    }

    val currentSort by vm.sort.collectAsState()
    val uiSort = when (currentSort) {
        SortBy.LATEST -> ReviewSort.Latest
        SortBy.LIKES  -> ReviewSort.Like
        SortBy.VIEWS  -> ReviewSort.View
    }

    //정렬 기준이 바뀔 때 스크롤 맨 위로 이동
    LaunchedEffect(sort) { gridState.scrollToItem(0) }

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
                    current = uiSort,
                    options = ReviewSort.entries.toList(),
                    label = { it.label },
                    onSelect = {
                        val serverSort = when (it) {
                            ReviewSort.Latest -> SortBy.LATEST
                            ReviewSort.Like   -> SortBy.LIKES
                            ReviewSort.View   -> SortBy.VIEWS
                        }
                        onChangeSort(serverSort)
                    }
                )
            }
            Spacer(Modifier.height(12.dp))

            when (state) {
                is UiState.Loading -> { Text("로딩 중…", style = MaterialTheme.typography.bodyMedium) }

                is UiState.Error -> {
                    val msg = (state as UiState.Error).message
                    Text("에러: $msg", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                }

                is UiState.Success -> {
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
                            ReviewCard(
                                review = review,
                                modifier = Modifier.clickable { onReviewClick(review) },
                                onSyncLikeCount = { next ->            // ★ NEW: 카드에서 좋아요 바꾸면
                                   // onSyncReviewLikeCount(review.id, next)  // 상위 reviews 동기화
                                    //vm.updateLocalLikeCount(review.id, next)
                                    //vm.toggleLike(review.id.toLong())
                                }
                            )
                        }
                        item { Spacer(Modifier.height(60.dp)) }
                    }
                }
                UiState.Idle -> Unit
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