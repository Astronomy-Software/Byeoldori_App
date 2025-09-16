package com.example.byeoldori.ui.components.community.freeboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.*
import com.example.byeoldori.viewmodel.Observatory.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeBoardDetail (
    post: FreePost,
    onBack: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
    currentUser: String
) {
    var input by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var editingTarget by remember { mutableStateOf<ReviewComment?>(null) }
    var requestKeyboard by remember { mutableStateOf(false) }
    var liked by rememberSaveable { mutableStateOf(setOf<String>()) }
    var postLiked by rememberSaveable { mutableStateOf(false) }
    var postLikeCount by rememberSaveable { mutableStateOf(post.likeCount) }

    LaunchedEffect(requestKeyboard) {
        if (requestKeyboard) {
            focusRequester.requestFocus()
            keyboardController?.show()
            requestKeyboard = false  // 한 번만 실행
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
                            createdAt = System.currentTimeMillis()
                        )
                    )
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
                Text(text = post.title, fontSize = 24.sp, color = TextHighlight)
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically
                ) {
                    val profilePainter = post.profile
                        ?.let { painterResource(id = it) }
                        ?: painterResource(id = R.drawable.profile1) // 기본 리소스 하나 준비

                    Icon(
                        painter = profilePainter,
                        contentDescription = "프로필",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(text = post.author, fontSize = 17.sp, color = TextHighlight)
                        Spacer(Modifier.height(4.dp))
                        Text( //작성일
                            text = formatCreatedAt(post.createdAt), // createdAt 형식에 맞게 변환 필요
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                            fontSize = 17.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                //Text(text = post.content, fontSize = 16.sp, color = TextHighlight)
                ContentInput(
                    items = post.contentItems,
                    onItemsChange = {},
                    onPickImages = {},
                    onSubmit = {},
                    onChecklist = {},
                    readOnly = true
                )
                Spacer(Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.6f), thickness = 2.dp)

                //좋아요 + 댓글
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            postLiked = !postLiked
                            postLikeCount = (postLikeCount + if (postLiked) 1 else -1).coerceAtLeast(0)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_thumbs_up),
                            contentDescription = "좋아요",
                            tint = if (postLiked) Purple500 else Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "좋아요   $postLikeCount",
                            color = TextHighlight,
                            fontSize = 14.sp
                        )
                    }
                    Divider(
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .height(20.dp)
                            .width(2.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_comment),
                            contentDescription = "댓글",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))

                        //총 댓글 수
                        val commentCount = dummyFreeComments.count { it.reviewId == post.id }
                        Text(
                            text = "댓글   $commentCount",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                Divider(color = Color.White.copy(alpha = 0.6f), thickness = 2.dp)
                dummyFreeComments
                    .filter { it.reviewId == post.id }
                    .forEach { fc ->
                        val isLiked = fc.id in liked
                        CommentItem(
                            comment = fc,
                            isLiked = isLiked,
                            onLike = {  tapped ->
                                val nowLiked = tapped.id !in liked
                                liked = if (nowLiked) liked + tapped.id else liked - tapped.id

                                val idx = dummyFreeComments.indexOfFirst { it.id == tapped.id }
                                if (idx >= 0) {
                                    val cur = dummyFreeComments[idx]
                                    val next = (cur.likeCount + if (nowLiked) +1 else -1).coerceAtLeast(0)
                                    dummyFreeComments[idx] = cur.copy(likeCount = next)
                                }
                            },
                            onReply = { /* TODO: 대댓글 입력창 열기 (추후) */ },
                            onEdit = { target ->
                                if (target.author == currentUser) {
                                    editingTarget = target        // 수정 모드
                                    input= ""
                                    requestKeyboard = true
                                }
                            },
                            onDelete = { del ->
                                val idx = dummyFreeComments.indexOfFirst { it.id == del.id }
                                if (idx >= 0) dummyFreeComments.removeAt(idx)
                            },
                            canEditDelete = { it.author == currentUser }
                        )
                    }
            }
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 420, heightDp = 840)
@Composable
private fun Preview_FreeBoardDetail() {
    val sample = remember { dummyFreePosts.first() }
    FreeBoardDetail(
        post = sample,
        onBack = {},
        onShare = {},
        onMore = {},
        currentUser = "astro_user"
    )
}


