package com.example.byeoldori.ui.components.community.freeboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.data.model.dto.FreePostResponse
import com.example.byeoldori.domain.Community.FreePost
import com.example.byeoldori.domain.Community.ReviewComment
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.mapper.toUi
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.CommunityViewModel
import com.example.byeoldori.viewmodel.UiState
import com.example.byeoldori.viewmodel.dummyFreeComments
import com.example.byeoldori.viewmodel.dummyFreePosts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeBoardDetail (
    post: FreePost, //더미 Post
    onBack: () -> Unit,
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
    currentUser: String,
    apiPost: FreePostResponse? = null, //api에서 받은 Post,
    vm: CommunityViewModel
) {
    var input by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var editingTarget by remember { mutableStateOf<ReviewComment?>(null) }
    var requestKeyboard by remember { mutableStateOf(false) }
    //var postLikeCount by rememberSaveable { mutableStateOf(post.likeCount) }
    var parent by remember { mutableStateOf<ReviewComment?>(null) }
    val likeState by vm.likeState.collectAsState()
    val likedIds by vm.likedIds.collectAsState()
    val likeKey = likedKeyFree(post.id)
    val isLiked = likeKey in likedIds


    var postLikeCount by rememberSaveable(post.id) {
        mutableStateOf(apiPost?.likeCount ?: post.likeCount)
    }

    LaunchedEffect(requestKeyboard) {
        if (requestKeyboard) {
            focusRequester.requestFocus()
            keyboardController?.show()
            requestKeyboard = false  // 한 번만 실행
        }
    }

    LaunchedEffect(likeState) {
        val s = likeState
        if (s is UiState.Success) {
            postLikeCount = s.data.likes.toInt()   // 서버 최종값으로 맞추기
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
                                contentDescription = "수정",
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
                Divider(color = Color.LightGray.copy(alpha = 0.4f), thickness = 1.dp)
            }
        },
        bottomBar = {
            CommentInput(
                text = input,
                onTextChange = { input = it },
                onSend = { raw ->
                    val t = raw.trim()
                    val target = editingTarget
                    if (target != null) {
                        val idx = dummyFreeComments.indexOfFirst { it.id == target.id }
                        if (idx >= 0) {
                            dummyFreeComments[idx] = target.copy(content = t)
                        }
                        // 모드 종료 + 입력 비우기
                        editingTarget = null
                        input = ""
                        return@CommentInput
                    }

                    // 자유게시판 댓글도 ReviewComment 더미를 재사용 (키는 post.id)
                    dummyFreeComments.add(
                        ReviewComment(
                            id = "c${System.currentTimeMillis()}",
                            reviewId = post.id,
                            author = currentUser,
                            profile = R.drawable.profile1,
                            content = t,
                            likeCount = 0,
                            commentCount = 0,
                            createdAt = System.currentTimeMillis(),
                            parentId = parent?.id
                        )
                    )
                    //대댓글
                    if(parent != null) {
                        val idx = dummyFreeComments.indexOfFirst { it.id == parent?.id }
                        if (idx >= 0) { //부모 댓글 찾으면
                            val cur = dummyFreeComments[idx]
                            val next = cur.copy(commentCount = cur.commentCount + 1)
                            dummyFreeComments[idx] = next
                        }
                        parent = null //대댓글 모드 해제
                    }
                    input = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .advancedImePadding() // 키보드 위에 바가 딱 붙도록
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
                //제목
                Text(text = apiPost?.title ?: post.title, fontSize = 24.sp, color = TextHighlight) //제목
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically
                ) {
                    val profilePainter = post.profile
                        ?.let { painterResource(id = it) }
                        ?: painterResource(id = R.drawable.profile1)

                    Icon(
                        painter = profilePainter,
                        contentDescription = "프로필",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column { //작성자
                        Text(text = apiPost?.authorId?.toString() ?: post.author, fontSize = 17.sp, color = TextHighlight)
                        Spacer(Modifier.height(4.dp))
                        Text( //작성일
                            text = apiPost?.createdAt?.toShortDate() ?: post.createdAt.toShortDate(),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                            fontSize = 17.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                ContentInput( //내용 입력(텍스트 + 이미지)
                    items = if (apiPost != null) {
                        listOf(
                            EditorItem.Paragraph(
                                value = TextFieldValue(apiPost.contentSummary)
                            )
                        )
                    } else {
                        post.contentItems.toUi()
                    },
                    onItemsChange = {},
                    onPickImages = {},
                    onCheck = {},
                    onChecklist = {},
                    readOnly = true
                )
                Spacer(Modifier.height(16.dp))

                //좋아요 + 댓글바
                LikeCommentBar(
                    key = likedKeyFree(post.id),
                    likeCount = postLikeCount,
                    onLikeCountChange = {},
                    onSyncLikeCount = { next ->
                        // 목록 원본 동기화(정렬/표시 일치)
//                        val idx = dummyFreePosts.indexOfFirst { it.id == post.id }
//                        if (idx >= 0) dummyFreePosts[idx] = dummyFreePosts[idx].copy(likeCount = next)

                        val key = likedKeyFree(post.id)
                        LikeState.ids = if (key in LikeState.ids) LikeState.ids - key else LikeState.ids + key
                        vm.toggleLikedLocal(likeKey)
                        vm.toggleLike(post.id.toLong())
                    },
                    commentCount = dummyFreeComments.count { it.reviewId == post.id }
                )

                //댓글 + 대댓글
                CommentList(
                    postId = post.id,
                    currentUser = currentUser,
                    comments = dummyFreeComments,
                    liked = LikeState.ids.filter { it.startsWith("freeComment:") }
                        .map { it.removePrefix("freeComment:") }.toSet(),
                    onLikedChange = { newLocal ->
                        // 로컬 댓글ID set을 전역 키 set으로 반영
                        val base = LikeState.ids.filterNot { it.startsWith("freeComment:") }.toSet()
                        LikeState.ids = base + newLocal.map { likedKeyFreeComment(it) }
                    },
                    onLike = {},
                    onReply = { target ->
                        parent = target
                        requestKeyboard = true
                    },
                    onEdit = { target ->
                        editingTarget = target
                        input = target.content
                        requestKeyboard = true
                    },
                    onDelete = { del ->
                        val idx = dummyFreeComments.indexOfFirst { it.id == del.id }
                        if (idx >= 0) dummyFreeComments.removeAt(idx)
                    }
                )
            }
        }
    }
}

//@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 420, heightDp = 840)
//@Composable
//private fun Preview_FreeBoardDetail() {
//    val sample = remember { dummyFreePosts.first() }
//    FreeBoardDetail(
//        post = sample,
//        onBack = {},
//        onShare = {},
//        onMore = {},
//        currentUser = "astro_user"
//    )
//}