package com.example.byeoldori.ui.screen.Community

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.dto.FreePostResponse
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.components.community.program.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.ui.components.community.freeboard.*
import com.example.byeoldori.ui.components.community.review.CommuReviewSection
import com.example.byeoldori.ui.components.community.review.ReviewDetail
import com.example.byeoldori.domain.Community.EduProgram
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.viewmodel.*
import com.example.byeoldori.ui.components.community.review.ReviewWriteForm

// --- 탭 정의 ---
enum class CommunityTab(val label: String, val routeSeg: String) {
    Home("홈","home"),
    Program("교육 프로그램", "program"),
    Review("관측 후기", "review"),
    Board("자유게시판", "board")
}

@Composable
fun CommunityScreen(
    tab: CommunityTab,
    onSelectTab: (CommunityTab) -> Unit,
    vm: CommunityViewModel = hiltViewModel()
) {
    val tabs = CommunityTab.entries
    var showWriteForm by remember { mutableStateOf(false) }
    val reviews = remember { mutableStateListOf<Review>().apply { addAll(dummyReviews) } }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastSubmittedReview by remember { mutableStateOf<Review?>(null) }
    var selectedReview by remember { mutableStateOf<Review?>(null) }
    var selectedFreePost by remember { mutableStateOf<String?>(null) }
    var selectedProgram by remember { mutableStateOf<EduProgram?>(null) }
    val currentUser = "헤이헤이"
    var showFreeBoardWriteForm by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val state by vm.postsState.collectAsState()
    val selectedId by vm.selectedPostId.collectAsState()
    val selectedPost by vm.selectedPost.collectAsState()
    val postDetailState by vm.postDetail.collectAsState()
    val currentSort by vm.sort.collectAsState()

    val reviewVm: ReviewViewModel = hiltViewModel()
    val detailState by reviewVm.detail.collectAsState()
    val apiDetail = (detailState as? UiState.Success)?.data
    val apiSummary = reviewVm.selectedPost.collectAsState().value

    LaunchedEffect(tab) {
        if (tab == CommunityTab.Board) {
            vm.loadPosts()
        }
    }
    LaunchedEffect(selectedId) {
        val idLong = selectedId?.toLongOrNull()
        if (idLong != null) vm.loadPostDetail(idLong)
    }

    when {
        showWriteForm -> {
            // 작성 화면만 표시 (탭/목록 숨김)
            ReviewWriteForm(
                author = currentUser,
                onCancel = {
                    showWriteForm = false
                    successMessage = "작성 취소되었습니다"
                    showSuccessDialog = true
                },   // 취소 → 다시 탭 화면으로
                onSubmit = {
                    showWriteForm = false
                    successMessage = "리뷰가 등록되었습니다"
                    showSuccessDialog = true
                    reviewVm.resetCreateState()
                },
                onTempSave = {},
                onMore = { /* 더보기 */ },
                vm = reviewVm,
                initialReview = null
            )

        }
        showFreeBoardWriteForm -> {
            FreeBoardWriteForm(
                author = currentUser,
                onCancel = {
                    showFreeBoardWriteForm = false
                    successMessage = "작성 취소되었습니다"
                    showSuccessDialog = true
                },
                onSubmit = {
                    showFreeBoardWriteForm = false
                    successMessage = "게시글이 등록되었습니다"
                    showSuccessDialog = true
                },
                onTempSave = {},
                onMore = {},
                onSubmitPost = { newPost ->
                    dummyFreePosts.add(0, newPost)
                    showFreeBoardWriteForm = false
                    showSuccessDialog = true
            },
                onClose = {}
            )
        }
        selectedReview != null -> {
            ReviewDetail(
                review = selectedReview!!,
                onBack = { selectedReview = null },  // 뒤로가기 누르면 다시 목록으로
                apiDetail = apiDetail,
                apiPost = apiSummary,
                currentUser = currentUser,
                onSyncReviewLikeCount = { id, next ->
                    val idx = reviews.indexOfFirst { it.id == id }
                    if (idx >= 0) {
                        reviews[idx] = reviews[idx].copy(likeCount = next)
                    }
                    //dummyReviews도 같이 갱신
                    val j = dummyReviews.indexOfFirst { it.id == id }
                    if (j >= 0) {
                        dummyReviews[j] = dummyReviews[j].copy(likeCount = next)
                    }
                },
                vm = reviewVm
            )
        }
        tab == CommunityTab.Board && selectedPost != null -> {
            val apiPost = (postDetailState as? UiState.Success)?.data
            FreeBoardDetail(
                post = selectedPost!!.toFreePost(),
                apiPost = apiPost,
                onBack = { vm.clearSelection() },
                currentUser = currentUser,
                vm = vm
            )
            return
        }

        selectedProgram != null -> {
            EduProgramDetail(
                program = selectedProgram!!,
                onBack = { selectedProgram = null },
                currentUser = currentUser
            )
        }

        else -> {
            Column(Modifier.fillMaxSize()) {
                // 탭바
                Column(
                    modifier = Modifier
                        .background(Blue800)
                ) {
                    Spacer(Modifier.height(24.dp))
                    ScrollableTabRow(
                        selectedTabIndex = tabs.indexOf(tab),
                        edgePadding = 0.dp,
                        containerColor = Blue800,
                        indicator = {} //강조선 제거
                    ) {
                        tabs.forEach { t ->
                            Tab(
                                selected = (t == tab),
                                onClick = { if (t != tab) onSelectTab(t) },
                                text = {
                                    Text(
                                        text = t.label,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                                        color = if (t == tab) TextHighlight else TextDisabled
                                    )
                                }
                            )
                        }
                    }
                }
                when (tab) {
                    CommunityTab.Review -> {
                        CommuReviewSection(
                            reviewsAll = reviews,
                            onWriteClick = {
                                reviewVm.resetCreateState()
                                showWriteForm = true
                            },
                            onReviewClick = { review ->
                                selectedReview = review
                                val idStr = review.id               // String
                                reviewVm.selectPost(idStr)          // selectPost(String)용

                                idStr.toLongOrNull()?.let { idL ->  // 상세 API는 Long 필요
                                    reviewVm.loadReviewDetail(idL)
                                }
                            },
                            onSyncReviewLikeCount = { id, next ->
                                val i = reviews.indexOfFirst { it.id == id }
                                if (i >= 0) reviews[i] = reviews[i].copy(likeCount = next)

                                //추가: 초기 소스도 함께 갱신
                                val j = dummyReviews.indexOfFirst { it.id == id }
                                if (j >= 0) dummyReviews[j] = dummyReviews[j].copy(likeCount = next)
                            }
                        )
                    }

                    CommunityTab.Program -> {
                        EduProgramSection(
                            eduProgramsAll = dummyPrograms,
                            onWriteClick = {}, //추후 추가
                            onClickProgram = { id ->
                                val program = dummyPrograms.find { it.id == id }
                                if (program != null) selectedProgram = program
                            }
                        )
                    }

                    CommunityTab.Home -> {
                        val recentReviews by remember { derivedStateOf { reviews.sortedByDescending { it.createdAt }.take(8) } }
                        val recentPrograms = remember { dummyPrograms.sortedByDescending { it.createdAt }.take(8) }
                        val popularFreePost = remember { dummyFreePosts.sortedByDescending { it.likeCount }.take(8) }

                        HomeSection(
                            recentReviews = recentReviews,
                            recentEduPrograms = recentPrograms,
                            popularFreePosts = popularFreePost,
                            onReviewClick = { selectedReview = it },
                            onProgramClick = { selectedProgram = it },
                            onFreePostClick = { selectedFreePost = it.id },
                            onSyncReviewLikeCount = { id, next ->
                                val i = reviews.indexOfFirst { it.id == id }
                                if (i >= 0) reviews[i] = reviews[i].copy(likeCount = next)

                                // (옵션) 초기 더미도 같이 갱신
                                val j = dummyReviews.indexOfFirst { it.id == id }
                                if (j >= 0) dummyReviews[j] = dummyReviews[j].copy(likeCount = next)
                            }
                        )
                    }

                    CommunityTab.Board -> {
                        when (state) {
                            is UiState.Idle, UiState.Loading -> {
                                CircularProgressIndicator()
                            }
                            is UiState.Success -> {
                                val posts = (state as UiState.Success<List<FreePostResponse>>).data
                                FreeBoardSection(
                                    freeBoardsAll = posts.map { it.toFreePost() },
                                    onClickPost = { id -> vm.selectPost(id) },
                                    onWriteClick = { showFreeBoardWriteForm = true },
                                    currentSort = currentSort,
                                    onChangeSort = { vm.setSort(it) },
                                    vm = vm
                                )
                            }
                            is UiState.Error -> {
                                val msg = (state as UiState.Error).message ?: "에러 발생"
                                Log.e("CommunityScreen", "자유게시판 에러: $msg")
                            }
                        }
                    }
                }
            }
        }
    }
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("확인")
                }
            },
            title = { Text("알림",color = Color.Black) },
            text = {
                Column {
                    //Text("정상적으로 등록되었습니다.", color = Color.Black)
                    Spacer(Modifier.height(8.dp))
                    Text(successMessage, color = Color.DarkGray)
                }
            }
        )
    }
}