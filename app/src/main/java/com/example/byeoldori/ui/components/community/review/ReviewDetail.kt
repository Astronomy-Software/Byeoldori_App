package com.example.byeoldori.ui.components.community.review

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Community.ReviewComment
import androidx.compose.ui.focus.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.domain.Content
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.mapper.toUi
import com.example.byeoldori.viewmodel.*
import com.example.byeoldori.viewmodel.Community.CommentsViewModel
import com.example.byeoldori.viewmodel.Community.ReviewViewModel
import kotlinx.coroutines.launch

@Composable
fun rememberIsImeVisible(): Boolean {
    val density = LocalDensity.current
    // 키보드가 올라오면 bottom 값이 0보다 커짐
    return WindowInsets.ime.getBottom(density) > 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetail(
    review: Review,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    currentUser: String,
    currentUserId: Long? = null,
    onSyncReviewLikeCount: (id: String, liked: Boolean, next: Int) -> Unit,
    apiDetail: ReviewDetailResponse? = null, // 서버에서 가져온 상세(요약/카운트)
    apiPost: ReviewResponse? = null,
    vm: ReviewViewModel? = null,
    commentsVm: CommentsViewModel? = null,
    onEdit: Boolean = true,
    onDelete: (reviewId: Long) -> Unit = {},
    onEditReview: (Review) -> Unit = {}
) {
    val imeVisible = rememberIsImeVisible()
    val tailRequester = remember { BringIntoViewRequester() } //키보드가 올라왔을 때 댓글창 숨어버리는 거 방지
    var requestKeyboard by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var input by rememberSaveable { mutableStateOf("") }
    var editingTarget by remember { mutableStateOf<ReviewComment?>(null) }
    var parent by remember { mutableStateOf<ReviewComment?>(null) }

    var reviewLikeCount by rememberSaveable { mutableStateOf(review.likeCount) }
    var liked by rememberSaveable { mutableStateOf(apiPost?.liked ?: review.liked) }

    val isPreview = LocalInspectionMode.current
    val commentsViewModel: CommentsViewModel? =
        commentsVm ?: if (!isPreview) hiltViewModel() else null

    val commentsState: UiState<List<ReviewComment>> by if (commentsViewModel != null) {
        commentsViewModel.comments.collectAsState()
    } else {
        remember { mutableStateOf<UiState<List<ReviewComment>>>(UiState.Idle) }
    }

    val commentCounts: Map<String, Int> by if (commentsViewModel != null) {
        commentsViewModel.commentCounts.collectAsState()
    } else {
        remember { mutableStateOf(emptyMap<String, Int>()) }
    }

    val commentList: List<ReviewComment> = when (val s = commentsState) {
        is UiState.Success -> s.data
        else -> emptyList()
    }

    val nonDeletedCount = remember(commentList) { commentList.count { !it.deleted } }
    val commentCountUi = when {
        nonDeletedCount > 0 -> nonDeletedCount
        else -> commentCounts[review.id] ?: 0
    }

    val detailUi: UiState<Review> by (vm?.detailUi?.collectAsState() ?: remember { mutableStateOf<UiState<Review>>(UiState.Idle) })
    val reviewForUi: Review = when(val s = detailUi) {
        is UiState.Success -> s.data //서버에서 받은 최신 상세 리뷰
        else -> review
    }

    val myId: Long? = currentUser.toLongOrNull()
    val myNick: String? = if (myId == null) currentUser else null

    var moreMenu by remember { mutableStateOf(false) } //더보기 누르면 수정/삭제 버튼
    var showDeleted by remember { mutableStateOf(false) } //삭제 확인 다이얼로그

    var deleteTarget by remember { mutableStateOf<ReviewComment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isOwner = remember(currentUserId, myNick, apiPost, reviewForUi) {
        when {
            apiPost?.authorId != null && currentUserId != null -> apiPost.authorId == currentUserId
            myNick != null -> reviewForUi.author == myNick
            else -> false
        }
    }

    LaunchedEffect(reviewForUi.id) { commentsViewModel?.start(reviewForUi.id) }

    LaunchedEffect(reviewForUi) {
        val photos = reviewForUi.contentItems.filterIsInstance<Content.Image.Url>()
        Log.d("DetailCheck", "이미지 개수: ${photos.size} -> ${photos.map { it.url }}")
    }

    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            // 키보드가 올라오면 리스트를 바닥까지 끌어내려 빈 여백 느낌 제거
            tailRequester.bringIntoView()
        }
    }
    LaunchedEffect(requestKeyboard) { //대댓글 버튼 눌렀을 때
        if (requestKeyboard) {
            focusRequester.requestFocus() //커서 깜빡임
            keyboardController?.show() //키보드 올라오게
            requestKeyboard = false  // 한 번만 실행
        }
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbar) { data ->
                Snackbar(
                    containerColor = Purple600,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(data.visuals.message)
                }
            }
        },
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_before),
                                contentDescription = "뒤로가기",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    },
                    title = {},
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                snackbar.showSnackbar(
                                    message = "아직 준비중인 기능입니다",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_constellation),
                                contentDescription = "공유",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))

                        Box {
                            IconButton(onClick = { moreMenu = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_more),
                                    contentDescription = "더보기",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = moreMenu,
                                onDismissRequest = { moreMenu = false }
                            ) {
                                if(onEdit && isOwner) {
                                    DropdownMenuItem(
                                        text = { Text("수정",color = Color.Black) },
                                        onClick = {
                                            moreMenu = false
                                            onEditReview(reviewForUi) //현재 리뷰 데이터를 넘김
                                        }
                                    )
                                    Divider(color = Color.Black.copy(alpha = 0.6f), thickness = 1.dp, modifier = Modifier.fillMaxWidth())
                                    DropdownMenuItem(
                                        text = { Text("삭제",color = Color.Black) },
                                        onClick = { showDeleted = true }
                                    )
                                }
                            }
                        }
                    }
                )
                Divider(
                    color = Color.White.copy(alpha = 0.6f),
                    thickness = 2.dp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                )
            }
        },
        bottomBar = {
            if (editingTarget == null) {
                CommentInput(
                    text = input,
                    onTextChange = { input = it },
                    onSend = { raw ->
                        val t = raw.trim()
                        if (t.isEmpty()) return@CommentInput
                        if (editingTarget != null) { //수정 모드
                            val targetId = editingTarget!!.id.toLongOrNull()
                            val postId = review.id.toLongOrNull()
                            if (targetId != null && postId != null) {
                                commentsViewModel?.update(
                                    postId = postId,
                                    commentId = targetId,
                                    content = t
                                ) {
                                    // 성공 콜백: 입력/대댓글 모드 해제
                                    input = ""
                                    parent = null
                                    vm?.loadPosts()
                                }
                            }
                        } else {
                            val parentIdStr = parent?.id
                            commentsViewModel?.submit(content = t, parentId = parentIdStr) {
                                // 성공 콜백: 입력/대댓글 모드 해제
                                input = ""
                                parent = null
                                vm?.loadPosts()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .advancedImePadding()
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .consumeWindowInsets(innerPadding)
                .advancedImePadding()
                .padding(bottom = 30.dp)
        ) {
            item {
                Spacer(Modifier.height(10.dp))
                Text( // 제목
                    text = reviewForUi.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextHighlight
                    )
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 프로필 이미지 (임시)
                        Icon(
                            painter = painterResource(R.drawable.profile1),
                            contentDescription = "프로필 이미지",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(50.dp)
                        )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text( //사용자 이름
                            text = reviewForUi.author,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            fontSize = 17.sp,
                            color = TextHighlight
                        )
                        Spacer(Modifier.height(4.dp))
                        Text( //작성일
                            text = reviewForUi.createdAt.toShortDate(),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                            fontSize = 17.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                ReviewInput(
                    target = reviewForUi.target.ifBlank { "—" },
                    onTargetChange = {},
                    site = reviewForUi.target.ifBlank { "—" },
                    onSiteChange = {},
                    equipment = reviewForUi.target.ifBlank { "—" },
                    onEquipmentChange = {},
                    date = reviewForUi.target.ifBlank { "—" },
                    onTimeChange = { _, _ -> },
                    rating = apiDetail?.review?.score?.let { "$it/5" }
                        ?: if (review.rating > 0) { "${review.rating}/5" } else { "미입력" },
                    onRatingChange = {},
                    onDateChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false //수정 못하게
                )
                val hasImages = hasHttpImage(reviewForUi.contentItems)
                val uiItems = reviewForUi.contentItems.toUi()
                if(hasImages) {
                    ContentInput(
                        items = reviewForUi.contentItems.toUi(),
                        onItemsChange = {},
                        onCheck = {},
                        onPickImages = {},
                        onChecklist = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ReadOnlyParagraphs(uiItems)
                }
                Spacer(Modifier.height(16.dp))
                // 좋아요 + 댓글 바
                LikeCommentBar(
                    likeCount = reviewLikeCount,
                    liked = liked,
                    onToggle = {
                        review.id.toLongOrNull()?.let { pid ->
                            vm?.toggleLike(pid) { res ->
                                liked = res.liked
                                reviewLikeCount = res.likes.toInt()
                                onSyncReviewLikeCount(review.id, res.liked, res.likes.toInt()) // ★ 상위 동기화
                            }
                        }
                    },
                   // onLikeCountChange = { reviewLikeCount = it },
                    onSyncLikeCount = {},
                    commentCount = commentCountUi //리뷰에 달린 댓글이 몇 개인지 계산
                )

                // 댓글 리스트
                CommentList(
                    postId = review.id,
                    currentUserId = myId,
                    currentUserNickname = myNick,
                    comments = commentList,
                    //현재 사용자가 좋아요를 누른 댓글들의 id 집합
                    liked = commentList.filter { it.liked }.map { it.id }.toSet(),
                    onLike = { tapped ->
                        tapped.id.toLongOrNull()?.let { cid ->
                            commentsViewModel?.toggleLike(cid)
                        }
                    },
                    onLikedChange = {},
                    onReply = { target ->
                        parent = target
                        requestKeyboard = true
                    },
                    onEdit = { target ->
                        editingTarget = target
                        input = ""
                    },
                    onDelete = { target ->
                        deleteTarget = target
                        showDeleteDialog = true
                    },
                    editingId = editingTarget?.id,
                    onSubmitEditInline = { target, newText ->
                        val postId = review.id.toLongOrNull()
                        val cid = target.id.toLongOrNull()
                        if (postId != null && cid != null && newText.isNotBlank()) {
                            commentsViewModel?.update(
                                postId = postId,
                                commentId = cid,
                                content = newText
                            ) {
                                editingTarget = null   // 저장 후 편집 종료
                                parent = null
                            }
                        }
                    },
                    onCancelEditInline = { editingTarget = null }
                )
            }
        }
    }
    if (showDeleted) {
        AlertDialog(
            onDismissRequest = { showDeleted = false },
            title = { Text("리뷰 삭제",color = Color.Black) },
            text = { Text("정말로 이 리뷰를 삭제하시겠어요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleted = false
                        moreMenu = false
                        onDelete(review.id.toLong())
                    }
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleted = false
                    moreMenu = false
                }) { Text("취소") }
            }
        )
    }
    if (showDeleteDialog && deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; deleteTarget = null },
            title = { Text("댓글 삭제", color = Color.Black) },
            text  = { Text("이 댓글을 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cid = deleteTarget!!.id.toLongOrNull()
                        if (cid != null) {
                            commentsViewModel?.delete(cid) {
                                // 성공 시 닫기
                                showDeleteDialog = false
                                deleteTarget = null
                                // 필요하면 상단 카운터/목록 갱신 트리거
                                vm?.loadPosts()
                            }
                        } else {
                            showDeleteDialog = false
                            deleteTarget = null
                        }
                    }
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; deleteTarget = null }) {
                    Text("취소")
                }
            }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 500, heightDp = 1200)
@Composable
private fun Preview_ReviewDetail() {
    MaterialTheme {
        ReviewDetail(review = dummyReviews.first(), currentUser = "astro_user",onSyncReviewLikeCount = { _, _ ,_-> })
    }
}