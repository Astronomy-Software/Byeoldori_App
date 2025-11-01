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
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.components.community.program.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.ui.components.community.freeboard.*
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.domain.Community.EduProgram
import com.example.byeoldori.domain.Community.FreePost
import com.example.byeoldori.domain.Content
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.viewmodel.*
import com.example.byeoldori.viewmodel.Community.*

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
    userVm: UserViewModel,
    vm: CommunityViewModel = hiltViewModel()
) {
    val me by userVm.userProfile.collectAsState(initial = null)
    val currentUserId = me?.id
    val currentNickname = me?.nickname ?: "익명"

    val tabs = CommunityTab.entries
    var showWriteForm by remember { mutableStateOf(false) }
    val reviews = remember { mutableStateListOf<Review>().apply { addAll(dummyReviews) } }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var selectedReview by remember { mutableStateOf<Review?>(null) }
    var selectedFreePost by remember { mutableStateOf<String?>(null) }
    var selectedProgram by remember { mutableStateOf<EduProgram?>(null) }
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

    val eduVm: EducationViewModel = hiltViewModel()
    val eduState by eduVm.postsState.collectAsState()
    val eduSelectedId by eduVm.selectedPostId.collectAsState()
    val eduSelectedPost by eduVm.selectedPost.collectAsState()
    val eduDetailState by eduVm.detail.collectAsState()
    val commentsVm: CommentsViewModel = hiltViewModel()

    var editReview by remember { mutableStateOf<Review?>(null) } //리뷰 수정 가능하게끔
    var editPost by remember { mutableStateOf<FreePost?>(null) }

    LaunchedEffect(selectedId) {
        val idLong = selectedId?.toLongOrNull()
        if (idLong != null) vm.loadPostDetail(idLong)
    }

    LaunchedEffect(eduSelectedId) {
        eduSelectedId?.toLongOrNull()?.let { eduVm.loadEducationDetail(it) }
    }

    LaunchedEffect(tab) {
        when (tab) {
            CommunityTab.Home -> {
                reviewVm.loadPosts()
                eduVm.loadPosts()
                vm.loadPosts()
            }
            CommunityTab.Review -> reviewVm.loadPosts()
            CommunityTab.Program -> eduVm.loadPosts()
            CommunityTab.Board -> vm.loadPosts()
        }
    }

    when {
        editReview != null -> { //수정 모드일 때
            ReviewWriteForm(
                currentUserId = currentUserId,
                author = currentNickname,
                vm = reviewVm,
                initialReview = editReview,            //수정 모드
                onCancel = {
                    editReview = null                  // 수정 취소
                },
                onSubmit = {
                    editReview = null                  // 수정 완료 후 닫기
                    successMessage = "리뷰가 수정되었습니다"
                    showSuccessDialog = true
                    reviewVm.loadPosts()
                },
                onTempSave = {},
                onMore = {}
            )
        }
        editPost != null -> {
            FreeBoardWriteForm(
                vm = vm,
                initialPost = editPost,                 // ← 초기값 주입 (수정 모드)
                onCancel = { editPost = null },         // 취소 → 닫기
                onSubmit = {
                    editPost = null                     // 저장 후 닫기
                    successMessage = "게시글이 수정되었습니다"
                    showSuccessDialog = true
                    vm.loadPosts()                      // 목록 갱신
                },
                onTempSave = {},
                onMore = {},
                onSubmitPost = {},
                onClose = { editPost = null }
            )
        }

        showWriteForm -> {
            // 작성 화면만 표시 (탭/목록 숨김)
            ReviewWriteForm(
                currentUserId = currentUserId,
                author = currentNickname,
                vm = reviewVm,
                initialReview = null, //작성모드
                onCancel = {
                    showWriteForm = false
                    successMessage = "작성 취소되었습니다"
                    showSuccessDialog = true
                },   // 취소 → 다시 탭 화면으로
                onSubmit = {
                    showWriteForm = false
                    successMessage = "리뷰가 등록되었습니다"
                    showSuccessDialog = true
                    reviewVm.loadPosts()
                    reviewVm.resetCreateState()
                },
                onTempSave = {},
                onMore = { /* 더보기 */ }
            )

        }
        showFreeBoardWriteForm -> {
            FreeBoardWriteForm(
                vm = vm,
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
                currentUser = currentNickname,
                currentUserId = currentUserId,
                onSyncReviewLikeCount = { id, liked, next ->
                    val idx = reviews.indexOfFirst { it.id == id }
                    if (idx >= 0) {
                        reviews[idx] = reviews[idx].copy(liked = liked, likeCount = next)
                    }
                },
                vm = reviewVm,
                onEdit = true, //수정 메뉴 보이도록
                onDelete = { id ->
                    vm.deletePost(id) {
                        selectedReview = null
                        reviewVm.loadPosts()
                        vm.loadPosts()
                    }
                },
                onEditReview = { review ->
                    editReview = review
                    selectedReview = null //상세 닫기
                    showWriteForm = false //수정 모드로 변경
                }
            )
        }
        selectedFreePost != null -> {
            val apiPost = (postDetailState as? UiState.Success)?.data
            val uiPost = selectedPost?.toFreePost()
                ?: dummyFreePosts.firstOrNull { it.id == selectedFreePost }
                ?: FreePost( // 최후 fallback (혹시라도 못 찾을 때)
                    id = selectedFreePost!!,
                    title = "",
                    author = "",
                    likeCount = 0,
                    commentCount = 0,
                    viewCount = 0,
                    createdAt = "",
                    contentItems = emptyList(),
                    profile = null,
                    liked = false
                )
            FreeBoardDetail(
                post = uiPost,
                apiPost = apiPost,
                onBack = {
                    selectedFreePost = null
                    vm.clearSelection()
                },
                vm = vm,
                onEdit = true,
                onDelete = { id ->
                    vm.deletePost(id) {
                        selectedFreePost = null
                        vm.loadPosts()
                    }
                },
                onEditPost = { post ->                //수정 진입
                    editPost = post                   // 편집 상태로 전환
                    selectedFreePost = null           // 상세 닫기
                    vm.clearSelection()
                }
            )
        }

        tab == CommunityTab.Board && selectedPost != null -> {
            val apiPost = (postDetailState as? UiState.Success)?.data
            FreeBoardDetail(
                post = selectedPost!!.toFreePost(),
                apiPost = apiPost,
                onBack = { vm.clearSelection() },
                vm = vm,
                onEdit = true,
                onDelete = { id ->
                    vm.deletePost(id) {
                        selectedFreePost = null
                        vm.loadPosts()
                    }
                },
                onEditPost = { post ->                //수정 진입
                    editPost = post                   // 편집 상태로 전환
                    vm.clearSelection()
                }
            )
        }

        selectedProgram != null -> {
            val programUi = selectedProgram!!
            EduProgramDetail(
                program = programUi,
                onBack = {
                    selectedProgram = null
                    eduVm.clearSelection()
                },
                currentUser = currentNickname,
                onStartProgram = { /* 필요 시 구현 */ },
                vm = eduVm,
                onEdit = true,
                onDelete = { id ->
                    id.toLongOrNull()?.let { pid ->
                        vm.deletePost(pid) {
                            selectedProgram = null
                            eduVm.loadPosts()
                        }
                    }
                }
            )
        }

        tab == CommunityTab.Program && eduSelectedPost != null -> {
            val apiPost = eduSelectedPost
            val programUi = apiPost!!.toEduProgram()

            EduProgramDetail(
                program = programUi,
                onBack = { eduVm.clearSelection() },
                currentUser = currentNickname,
                onStartProgram = { /* 필요 시 구현 */ },
                vm = eduVm,
                onEdit = true,
                onDelete = { id ->
                    id.toLongOrNull()?.let { pid ->
                        vm.deletePost(pid) {
                            selectedProgram = null
                            eduVm.loadPosts()
                        }
                    }
                }
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
                            vm = reviewVm,
                            reviewsAll = reviews,
                            onWriteClick = {
                                reviewVm.resetCreateState()
                                showWriteForm = true
                            },
                            onReviewClick = { review ->
                                selectedReview = review
                                val idStr = review.id
                                reviewVm.selectPost(idStr)

                                idStr.toLongOrNull()?.let { idL ->
                                    reviewVm.loadReviewDetail(idL)
                                }
                            },
                            onSyncReviewLike = { id, liked, next ->
                                val i = reviews.indexOfFirst { it.id == id }
                                if (i >= 0) reviews[i] = reviews[i].copy(likeCount = next, liked = liked)
                            },
                        )
                    }
                    CommunityTab.Program -> {
                        when (eduState) {
                            is UiState.Idle, UiState.Loading -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            is UiState.Success -> {
                                val posts = (eduState as UiState.Success<List<EducationResponse>>).data
                                val counts by commentsVm.commentCounts.collectAsState()

                                EduProgramSection(
                                    eduProgramsAll = posts.map { it.toEduProgram() },
                                    onWriteClick = { /* 교육 글쓰기 있으면 연결 */ },
                                    onClickProgram = { id ->
                                        eduVm.selectPost(id)           // 상세 선택
                                    },
                                    vm = eduVm,                        // (필요 시 내부에서 sort/search 사용)
                                    commentsVm = commentsVm
                                )
                            }
                            is UiState.Error -> {
                                val msg = (eduState as UiState.Error).message ?: "에러 발생"
                                Log.e("CommunityScreen", "교육 프로그램 에러: $msg")
                            }
                        }
                    }

                    CommunityTab.Home -> {
                        val reviewList = when (val s = reviewVm.postsState.collectAsState().value) {
                            is UiState.Success -> s.data.map { it.toReview() }
                            else -> emptyList()
                        }
                        val eduList = when (val s = eduVm.postsState.collectAsState().value) {
                            is UiState.Success -> s.data.map { it.toEduProgram() }
                            else -> emptyList()
                        }
                        val freeList = when (val s = vm.postsState.collectAsState().value) {
                            is UiState.Success -> s.data.map { it.toFreePost() }
                            else -> emptyList()
                        }

                        HomeSection(
                            recentReviews = reviewList.take(20),
                            recentEduPrograms = eduList.take(20),
                            popularFreePosts = freeList.sortedByDescending { it.likeCount }.take(20),
                            onReviewClick = { review ->
                                selectedReview = review
                                reviewVm.selectPost(review.id)
                                review.id.toLongOrNull()?.let { reviewVm.loadReviewDetail(it) }
                            },
                            onProgramClick = { program ->
                                selectedProgram = program
                                eduVm.selectPost(program.id)
                                program.id.toLongOrNull()?.let { eduVm.loadEducationDetail(it) }
                            },
                            onFreePostClick = { postId ->
                                selectedFreePost = postId
                                vm.selectPost(postId)
                                postId.filter(Char::isDigit).toLongOrNull()?.let { idL ->
                                    vm.loadPostDetail(idL)
                                } ?: Log.w("CommunityScreen", "Cannot parse postId to Long: $postId")
                            },
                            onSyncReviewLikeCount = { id, next ->
                                val i = reviews.indexOfFirst { it.id == id }
                                if (i >= 0) reviews[i] = reviews[i].copy(likeCount = next)
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
                    Spacer(Modifier.height(8.dp))
                    Text(successMessage, color = Color.DarkGray)
                }
            }
        )
    }
}