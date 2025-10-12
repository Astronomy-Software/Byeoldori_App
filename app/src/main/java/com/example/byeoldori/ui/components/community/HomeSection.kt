package com.example.byeoldori.ui.components.community

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.domain.Community.EduProgram
import com.example.byeoldori.domain.Community.FreePost
import com.example.byeoldori.ui.components.community.freeboard.asReview
import com.example.byeoldori.ui.components.community.program.asReview
import com.example.byeoldori.ui.components.observatory.ReviewCard
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.viewmodel.Community.CommentsViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeSection(
    recentReviews: List<Review>,
    recentEduPrograms: List<EduProgram>,
    popularFreePosts: List<FreePost>,
    onReviewClick: (Review) -> Unit = {},
    onProgramClick: (EduProgram) -> Unit = {},
    //onFreePostClick: (FreePost) -> Unit = {},
    onFreePostClick: (String) -> Unit = {},
    onSyncReviewLikeCount: (id: String, next: Int) -> Unit,
    enableInternalScroll: Boolean = true,   //추가
    internalPadding: Dp = 16.dp
) {
    val commentsVm: CommentsViewModel = hiltViewModel()
    val commentCounts by commentsVm.commentCounts.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            //.verticalScroll(rememberScrollState())
            //.padding(16.dp)
            .then(if (enableInternalScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(internalPadding)
    ) {
        PagerSection(
            title = "최근 추가된 관측지 리뷰",
            items = recentReviews,
            itemContent = { review ->
                Box(Modifier.clickable { onReviewClick(review) }) {
                    ReviewCard(
                        review = review,
                        commentCount = commentCounts[review.id] ?: review.commentCount,
                        onSyncLikeCount = { next ->
                            onSyncReviewLikeCount(review.id, next)
                        }
                    )
                }
            }
        )
        PagerSection(
            title = "새로운 교육 프로그램",
            items = recentEduPrograms,
            itemContent = { program ->
                Box(Modifier.clickable { onProgramClick(program) }) {
                    ReviewCard(review = program.asReview())
                }
            }
        )
        PagerSection(
            title = "인기 자유게시판 게시물",
            items = popularFreePosts,
            itemContent = { post ->
                ReviewCard(
                    review = post.asReview(),
                    commentCount = post.commentCount,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            Log.d("HomeSection", "free click id=${post.id}")
                            onFreePostClick(post.id) // (String) -> Unit 이므로 id만 전달
                        }
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun <T> PagerSection(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    onMoreClick: () -> Unit = {},
    columns: Int = 2,
    rows: Int = 2,
    itemContent: @Composable (T) -> Unit
) {
    val pageSize = (columns * rows).coerceAtLeast(1)
    val pageCount = if (items.isEmpty()) 1 else ((items.size - 1) / pageSize + 1)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val prevEnabled = pagerState.currentPage > 0
    val nextEnabled = pagerState.currentPage < pageCount - 1

    Column(modifier.fillMaxWidth()) {
        Text(title,color= TextHighlight, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(540.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                modifier = Modifier
                    .matchParentSize()
            ) { page ->
                val start = page * pageSize
                val pageItems = items.drop(start).take(pageSize)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pageItems) { item ->
                        itemContent(item)
                    }
                }
            }
            //이전 버튼
            IconButton(
                onClick = {
                    scope.launch {
                        val prev = (pagerState.currentPage - 1).coerceAtLeast(0)
                        pagerState.animateScrollToPage(prev)
                    }
                },
                enabled = prevEnabled,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-25).dp, y = (-22).dp)
                    .size(50.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_before),
                    contentDescription = "이전",
                    tint = if (prevEnabled) Color.White else Color.Gray
                )
            }
            //다음 버튼
            IconButton(
                onClick = {
                    scope.launch {
                        val next = (pagerState.currentPage + 1).coerceAtMost(pageCount - 1)
                        pagerState.animateScrollToPage(next)
                    }
                },
                enabled = nextEnabled,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 25.dp, y = (-22).dp)
                    .size(50.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_next),
                    contentDescription = "다음",
                    tint = if (nextEnabled) Color.White else Color.Gray
                )
            }
        }
    }
}