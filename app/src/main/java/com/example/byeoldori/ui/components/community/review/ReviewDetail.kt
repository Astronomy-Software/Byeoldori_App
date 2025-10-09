package com.example.byeoldori.ui.components.community.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.*
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
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.mapper.toUi
import com.example.byeoldori.viewmodel.*

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
    onMore: () -> Unit = {},
    currentUser: String,
    onSyncReviewLikeCount: (id: String, liked: Boolean, next: Int) -> Unit,
    apiDetail: ReviewDetailResponse? = null, // 서버에서 가져온 상세(요약/카운트)
    apiPost: ReviewResponse? = null,
    vm: ReviewViewModel? = null
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

    val commentsVm: CommentsViewModel = hiltViewModel()
    val commentsState by commentsVm.comments.collectAsState()
    val commentCounts by commentsVm.commentCounts.collectAsState()

    val reviewComments = remember { mutableStateListOf<ReviewComment>() }

    val commentCountUi = commentCounts[review.id] ?: when (val s = commentsState) {
        is UiState.Success -> s.data.size
        else -> 0
    }

    val commentList: List<ReviewComment> = when (val s = commentsState) {
        is UiState.Success -> s.data
        else -> emptyList()
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

    //댓글 로드
    LaunchedEffect(review.id) {
        commentsVm.start(review.id)  // 서버에서 page=0부터 불러옴
    }

    //댓글 목록을 화면 버퍼에 반영
    LaunchedEffect(commentsState) {
        (commentsState as? UiState.Success)?.let { s ->
            reviewComments.clear()
            reviewComments.addAll(s.data)
        }
    }

    Scaffold(
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
                        IconButton(onClick = onShare) {
                            Icon(
                                painter = painterResource(R.drawable.ic_constellation),
                                contentDescription = "공유",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        IconButton(onClick = onMore) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more),
                                contentDescription = "더보기",
                                tint = Color.White
                            )
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
            CommentInput(
                text = input,
                onTextChange = { input = it },
                onSend = { raw ->
                    val t = raw.trim()
                    val target = editingTarget
                    if (target != null) { //기존 댓글 수정
                        val idx = dummyReviewComments.indexOfFirst { it.id == target.id }
                        if (idx >= 0) {
                            dummyReviewComments[idx] = target.copy(content = t)
                        }
                        editingTarget = null
                        input = ""
                        return@CommentInput
                    }
                    // 새 댓글 추가
                    dummyReviewComments.add(
                        ReviewComment(
                            id = "c${System.currentTimeMillis()}",
                            reviewId = review.id,
                            author = currentUser,
                            profile = R.drawable.profile1,
                            content = t,
                            likeCount = 0,
                            commentCount = 0,
                            createdAt =  review.createdAt.toShortDate(),
                            parentId = parent?.id
                        )
                    )
                    //대댓글
                    if(parent != null) {
                        val idx = dummyReviewComments.indexOfFirst { it.id == parent?.id }
                        if (idx >= 0) { //부모 댓글 찾으면
                            val cur = dummyReviewComments[idx]
                            val next = cur.copy(commentCount = cur.commentCount + 1) //copy를 이용해 새로운 객체 생성
                            dummyReviewComments[idx] = next //기존 댓글을 새로운 객체로 교체
                        }
                        parent = null //대댓글 모드 해제
                    }
                    input = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .advancedImePadding()
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            item {
                Spacer(Modifier.height(10.dp))
                Text( // 제목
                    text = apiPost?.title ?: review.title,
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
                            text = apiPost?.authorNickname ?: review.author,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            fontSize = 17.sp,
                            color = TextHighlight
                        )
                        Spacer(Modifier.height(4.dp))
                        Text( //작성일
                            text = (apiPost?.createdAt ?: review.createdAt.toShortDate()).let {
                                if (apiPost != null) it.toShortDate() else it
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                            fontSize = 17.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                ReviewInput(
                    target = apiDetail?.review?.target ?: review.target?.takeIf { it.isNotBlank() } ?: "—",
                    onTargetChange = {},
                    site = apiDetail?.review?.location ?: review.site?.takeIf { it.isNotBlank() } ?: "—",
                    onSiteChange = {},
                    equipment = apiDetail?.review?.equipment ?: review.equipment?.takeIf { it.isNotBlank() } ?: "—",
                    onEquipmentChange = {},
                    date = apiDetail?.review?.observationDate ?: review.date?.takeIf { it.isNotBlank() } ?: "—",
                    onTimeChange = { _, _ -> },
                    rating = apiDetail?.review?.score?.let { "$it/5" }
                        ?: if (review.rating > 0) { "${review.rating}/5" } else { "미입력" },
                    onRatingChange = {},
                    onDateChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false //수정 못하게
                )
               // Divider(color = Color.LightGray.copy(alpha = 0.4f), thickness = 1.dp)
                ContentInput(
                    items = when {
                        apiPost?.contentSummary != null -> listOf(
                            EditorItem.Paragraph(value = TextFieldValue(apiPost.contentSummary))
                        )
                        apiDetail?.content != null -> listOf(
                            EditorItem.Paragraph(value = TextFieldValue(apiDetail.content))
                        )
                        else -> review.contentItems.toUi()
                    },
                    onItemsChange = {},
                    onCheck = {},
                    onPickImages = {},
                    onChecklist = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                // 좋아요 + 댓글 바
                LikeCommentBar(
                    key = likedKeyReview(review.id),
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
                    currentUser = currentUser,
                    comments = reviewComments,
                    //현재 사용자가 좋아요를 누른 댓글들의 id 집합
                    liked = LikeState.ids.filter { it.startsWith("reviewComment:") }
                        .map { it.removePrefix("reviewComment:") }.toSet(),
                    onLikedChange = { newLocal ->
                        val base = LikeState.ids.filterNot { it.startsWith("reviewComment:") }.toSet()
                        LikeState.ids = base + newLocal.map { likedKeyReviewComment(it) }
                    },
                    onLike = {},
                    onReply = { target ->
                        parent = target
                        requestKeyboard = true
                    },
                    onEdit = { target ->
                        editingTarget = target
                        input = ""
                        requestKeyboard = true
                    },
                    onDelete = { del ->
                        val idx = dummyReviewComments.indexOfFirst { it.id == del.id }
                        if (idx >= 0) dummyReviewComments.removeAt(idx)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 500, heightDp = 1200)
@Composable
private fun Preview_ReviewDetail() {
    MaterialTheme {
        ReviewDetail(review = dummyReviews.first(), currentUser = "astro_user",onSyncReviewLikeCount = { _, _ ,_-> })
    }
}