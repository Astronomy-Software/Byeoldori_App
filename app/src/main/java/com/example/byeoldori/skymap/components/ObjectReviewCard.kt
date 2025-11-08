package com.example.byeoldori.skymap.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.components.community.review.toReview
import com.example.byeoldori.ui.components.observatory.ReviewSection
import com.example.byeoldori.ui.theme.TextDisabled
import com.example.byeoldori.viewmodel.Community.*
import com.example.byeoldori.viewmodel.UiState

@Composable
fun ObjectReviewCard(
    objectName: String,
    starVm: StarViewModel = hiltViewModel(),
    reviewVm: ReviewViewModel = hiltViewModel(),
    commentsVm: CommentsViewModel = hiltViewModel(),
    onReviewClick: (Triple<Review, ReviewResponse?, ReviewDetailResponse?>) -> Unit
) {

    val reviewsState by starVm.reviewsState.collectAsState()

    LaunchedEffect(objectName) { //천체 이름이 바뀔 때마다 후기 요청
        starVm.loadObjectReviews(objectName)
    }

    when (reviewsState) {
        is UiState.Loading -> {
            Text("관측 후기를 불러오는 중...",color = TextDisabled)
            Spacer(Modifier.height(8.dp))
        }
        is UiState.Error -> {
            Text("관측 후기 조회 실패",color = TextDisabled)
        }
        is UiState.Success -> {
            val apiList = (reviewsState as UiState.Success<List<ReviewResponse>>).data

            if (apiList.isEmpty()) {
                Text("이 천체에 대한 관측 후기가 아직 없어요", color = TextDisabled)
                Spacer(Modifier.height(8.dp))
            } else {
                val baseUi = remember(apiList) { apiList.map { it.toReview() } }
                val commentCounts by commentsVm.commentCounts.collectAsState()
                val scoreMap = remember(apiList) { apiList.associate { it.id.toString() to (it.score ?: 0) } }
                val uiReviews = remember(baseUi, commentCounts, scoreMap) {
                    baseUi.map { r ->
                        val injectedRating = scoreMap[r.id]?.toInt() ?: r.rating
                        r.copy(
                            commentCount = commentCounts[r.id] ?: r.commentCount,
                            rating = injectedRating
                        )
                    }
                }

                var pending by remember { mutableStateOf<Pair<Review, ReviewResponse?>?>(null) }
                val detailState by reviewVm.detail.collectAsState()

                LaunchedEffect(detailState, pending) {
                    val (ui, apiPost) = pending ?: return@LaunchedEffect
                    if (detailState is UiState.Success) {
                        onReviewClick(Triple(ui, apiPost, (detailState as UiState.Success<ReviewDetailResponse>).data))
                        pending = null
                    }
                }

                ReviewSection(
                    title = "관측 후기",
                    reviews = uiReviews,
                    onSyncReviewLikeCount =  { _, _ -> },
                    onReviewClick = { clickedUi ->
                        val id = clickedUi.id.toLongOrNull() ?: return@ReviewSection
                        pending = clickedUi to apiList.firstOrNull { it.id == id }
                        reviewVm.loadReviewDetail(id)
                    },
                    onToggleLike = {}
                )
            }
        }
        else -> Unit
    }
}