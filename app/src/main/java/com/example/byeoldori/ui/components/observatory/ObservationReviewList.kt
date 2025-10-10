package com.example.byeoldori.ui.components.observatory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.dto.ReviewDetailResponse
import com.example.byeoldori.data.model.dto.ReviewResponse
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.components.community.review.toReview
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.ReviewViewModel
import com.example.byeoldori.viewmodel.UiState

//해당 관측지에서 작성항 리뷰들만 필터링
@Composable
fun ObservationReviewList(
    siteId: Long,
    vm: ReviewViewModel = hiltViewModel(),
    onReviewClick: (Triple<Review, ReviewResponse?, ReviewDetailResponse?>) -> Unit
) {
    val postState by vm.postsState.collectAsState()
    val allReviews = when(postState) {
        is UiState.Success -> (postState as UiState.Success<List<ReviewResponse>>).data
        else -> emptyList()
    }

    val siteReviews = remember(allReviews, siteId) {
        allReviews.filter { it.observationSiteId == siteId }.map { it.toReview() }
    }

    var pending by remember { mutableStateOf<Pair<Review, ReviewResponse?>?>(null) }
    val detailState by vm.detail.collectAsState()

    // 상세가 성공하면 콜백 1회 호출하고 pending 해제
    LaunchedEffect(detailState, pending) {
        val (ui, apiPost) = pending ?: return@LaunchedEffect
        if (detailState is UiState.Success) {
            onReviewClick(Triple(ui, apiPost, (detailState as UiState.Success).data))
            pending = null
        }
    }

    if(siteReviews.isEmpty()) {
        Text("해당 관측지에서 진행한 후기", color = TextHighlight)
        Spacer(Modifier.height(10.dp))
        Text("아직 관측 후기가 없습니다", color = TextDisabled)
    } else {
        ReviewSection(
            title = "해당 관측지에서 진행한 관측후기",
            reviews = siteReviews,
            onSyncReviewLikeCount = { id, next -> /* 필요시 상위 동기화 */ },
            onReviewClick = { clickedUi ->
                val id = clickedUi.id.toLongOrNull() ?: return@ReviewSection
                val apiPost = allReviews.firstOrNull { it.id == id }
                pending = clickedUi to apiPost
                vm.loadReviewDetail(id)
            }
        )
    }
}