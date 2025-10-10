package com.example.byeoldori.ui.components.community.review

import androidx.compose.foundation.*
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
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.domain.Content
import com.example.byeoldori.ui.components.community.SortBar
import com.example.byeoldori.ui.components.observatory.ReviewCard
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.components.community.freeboard.formatCreatedAt
import com.example.byeoldori.viewmodel.*
import com.example.byeoldori.viewmodel.Community.CommentsViewModel
import com.example.byeoldori.viewmodel.Community.ReviewViewModel

// 정렬 타입
enum class ReviewSort(val label: String) {
    Latest("최신순"), Like("좋아요순"), View("조회수순")
}

fun ReviewResponse.toReview(): Review = Review(
    id = id.toString(),
    title = title,
    author = authorNickname ?: "익명",
    profile = R.drawable.profile1, // 서버에 프로필 리소스 없으면 null 유지
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
    rating = 0,
    siteScore = 0,
    liked = liked
)


// Review 탭 콘텐츠
@Composable
fun CommuReviewSection(
    vm: ReviewViewModel? = null,
    reviewsAll: List<Review>,
    onWriteClick: () -> Unit = {},
    onReviewClick: (Review) -> Unit,
    onChangeSort: (SortBy) -> Unit = {},
    onSyncReviewLike: (id: String, liked: Boolean, next: Int) -> Unit,
    selectedSiteId: Long? = null
) {
    var searchText by remember { mutableStateOf("") } //초기값이 빈 문자열인 변할 수 있는 상태 객체
    var sort by remember { mutableStateOf(ReviewSort.Latest) }
    val gridState = rememberLazyGridState()

    val state by (vm?.postsState?.collectAsState() ?: remember { mutableStateOf<UiState<List<ReviewResponse>>>(UiState.Idle) })
    val scores by (vm?.scores?.collectAsState() ?: remember { mutableStateOf<Map<String, Int>>(emptyMap()) })
    val currentSort by (vm?.sort?.collectAsState() ?: remember { mutableStateOf(SortBy.LATEST) })

    val networkList: List<Review> = when (state) {
        is UiState.Success -> (state as UiState.Success<List<ReviewResponse>>)
            .data.map { it.toReview() }
        else -> emptyList()
    }
    val baseList = if (networkList.isNotEmpty()) networkList else reviewsAll

    val merged = remember(baseList, scores) {
        baseList.map { r -> r.copy(rating = scores[r.id] ?: r.rating) }
    }

    val commentsVm: CommentsViewModel = hiltViewModel()
    val commentCounts by commentsVm.commentCounts.collectAsState()

    val mergedWithCounts = remember(merged, commentCounts) {
        merged.map { r -> r.copy(commentCount = commentCounts[r.id] ?: r.commentCount) }
    }

    val filtered = remember(searchText, mergedWithCounts) {
        if (searchText.isBlank())  mergedWithCounts
        else {
            val k = searchText.trim().lowercase()
            mergedWithCounts.filter { r ->
                r.title.lowercase().contains(k) ||
                        r.author.lowercase().contains(k) ||
                        r.contentItems.filterIsInstance<Content.Text>()
                            .any { it.text.lowercase().contains(k) }
            }
        }
    }
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
                        sort = it
                        val serverSort = when (it) {
                            ReviewSort.Latest -> SortBy.LATEST
                            ReviewSort.Like   -> SortBy.LIKES
                            ReviewSort.View   -> SortBy.VIEWS
                        }
                        vm?.setSort(serverSort)
                    }
                )
            }
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2), //컬럼 개수 2개
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = filtered, key = { it.id }) { review ->
                    LaunchedEffect(review.id) {
                        review.id.toLongOrNull()?.let { vm?.ensureScoreLoaded(it) }
                    }
                    ReviewCard(
                        review = review,
                        modifier = Modifier.clickable { onReviewClick(review) },
                        onSyncLikeCount = { next ->
                        // onSyncReviewLikeCount(review.id, next)  // 상위 reviews 동기화
                        //vm.updateLocalLikeCount(review.id, next)
                        //vm.toggleLike(review.id.toLong())
                        },
                        onToggleLike = {
                            review.id.toLongOrNull()?.let { pid ->
                                vm?.toggleLike(pid) { result ->
                                    onSyncReviewLike(review.id, result.liked, result.likes.toInt()) // ★
                                }
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(60.dp)) }
            }
        }
        when (state) {
            is UiState.Loading -> Text(
                "로딩 중…",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
            is UiState.Error -> Text(
                "에러: ${(state as UiState.Error).message}",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
            else -> Unit
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

@Preview(showBackground = true, widthDp = 360, heightDp = 840)
@Composable
private fun Preview_ScreenWithWriteButton() {
    MaterialTheme {
        CommuReviewSection(
            vm = null,
            reviewsAll = dummyReviews,
            onWriteClick = {  },
            onReviewClick = { review ->
                println("리뷰 클릭됨: ${review.title}")
            },
            onChangeSort = { },
            onSyncReviewLike = { id, liked, next ->
                val idx = dummyReviews.indexOfFirst { it.id == id }
                if (idx >= 0) {
                    dummyReviews[idx] = dummyReviews[idx].copy(likeCount = next, liked = liked)
                }
            }
        )
    }
}