package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.data.model.dto.ReviewResponse
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.viewmodel.Community.*
import com.example.byeoldori.viewmodel.UiState

@Composable
fun MyReviewList(
    onBack: () -> Unit = {},
    onDetailModeChange: (Boolean) -> Unit = {},
    reviewVm: ReviewViewModel = hiltViewModel(),
    vm: CommunityViewModel = hiltViewModel(),
    userVm: UserViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        userVm.getMyProfile()
        reviewVm.loadPosts()
    }
    val me = userVm.userProfile.collectAsState().value
    val myId = me?.id
    val nickname = me?.nickname ?: "익명"

    val postState by reviewVm.postsState.collectAsState()
    val detailState by reviewVm.detail.collectAsState()
    val apiDetail = (detailState as? UiState.Success)?.data
    val apiSummary = reviewVm.selectedPost.collectAsState().value

    val allReviews: List<ReviewResponse> = when (postState) {
        is UiState.Success -> (postState as UiState.Success<List<ReviewResponse>>).data
        else -> emptyList()
    }

    val myReviews = remember(allReviews,myId) {
        if(myId == null) emptyList() else allReviews.filter { it.authorId == myId }
    }

    var selectedReview by remember { mutableStateOf<Review?>(null) }
    var editingReview    by remember { mutableStateOf<Review?>(null) }

    //상세 화면 노출 여뷰를 부모에 알려줌
    LaunchedEffect(selectedReview, editingReview) {
        onDetailModeChange(selectedReview != null || editingReview != null)
    }

    LaunchedEffect(selectedReview?.id) {
        val idLong = selectedReview?.id?.toLongOrNull()
        if (idLong != null) reviewVm.loadReviewDetail(idLong)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            editingReview != null -> {
                val draft = editingReview!!
                ReviewWriteForm(
                    author = draft.author,
                    onCancel = { editingReview = null; selectedReview = null },
                    onSubmit = {
                        editingReview = null
                        selectedReview = null
                        reviewVm.loadPosts()
                    },
                    onTempSave = {},
                    onMore = {},
                    initialReview = draft,
                    vm = reviewVm
                )
            }

            selectedReview != null -> {
                ReviewDetail(
                    review = selectedReview!!,
                    currentUser = nickname,
                    currentUserId = myId,
                    onBack = { selectedReview = null },
                    onEditReview = { editable ->
                        editingReview = editable
                    },
                    onDelete = { id ->
                        vm.deletePost(id) {
                            selectedReview = null
                            vm.loadPosts()
                        }
                    },
                    vm = reviewVm,
                    apiDetail = apiDetail,
                    apiPost = apiSummary,
                    onSyncReviewLikeCount = { _, _, _ -> },
                )
            }
            else -> {
                ReviewGrid(
                    reviews = myReviews.map { it.toReview() },
                    onClickReview = { selectedReview = it },
                    onToggleLike = { id ->
                        reviewVm.toggleLike(id.toLong()) { reviewVm.loadPosts() }
                    }
                )
            }
        }
    }
}