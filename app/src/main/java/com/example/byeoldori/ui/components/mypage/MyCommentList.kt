package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.domain.Community.*
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.*
import com.example.byeoldori.viewmodel.UiState
import kotlinx.coroutines.*
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCommentList(
    onBack: () -> Unit = {},
    reviewVm: ReviewViewModel = hiltViewModel(),
    userVm: UserViewModel = hiltViewModel(),
    commentsVm: CommentsViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        userVm.getMyProfile()
        reviewVm.loadPosts() // 리뷰 목록 선로딩
    }

    val me = userVm.userProfile.collectAsState().value
    val myId = me?.id ?: -1L
    val reviewState by reviewVm.postsState.collectAsState()
    var groups by remember { mutableStateOf<List<MyCommentGroup>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(myId,reviewState) {
        if(myId <= 0) return@LaunchedEffect
        loading = true; error = null
        runCatching {
            val posts = (reviewState as? UiState.Success)?.data.orEmpty()
            if(posts.isEmpty()) emptyList<MyCommentGroup>() else coroutineScope {
                posts.map { p->
                    async {
                        val all = commentsVm.AllMyReviewComments(p.id)
                        val mine = all.filter { it.authorId == myId && !it.deleted }
                        if(mine.isEmpty()) null else MyCommentGroup(
                            source = CommentSourceType.REVIEW,
                            postId = p.id,
                            postTitle = p.title,
                            postAuthorName = p.authorNickname ?: "익명",
                            postCreatedAt = p.createdAt,
                            myComments = mine.map {
                                MyCommentUi(
                                    source = CommentSourceType.REVIEW,
                                    postId = p.id,
                                    postTitle = p.title,
                                    postAuthorName = p.authorNickname ?: "익명",
                                    commentId = it.id,
                                    content = it.content ?: "",
                                    createdAt = it.createdAt
                                )
                            }
                        )
                    }
                }.mapNotNull { it.await() }
            }
        }.onSuccess { groups = it; loading = false }
            .onFailure { e-> error = e.message ?: "리뷰 댓글 불러오는 중 오류"; loading = false }
    }

    Background(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "내가 작성한 댓글",
                        color = TextHighlight,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.3f
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_before),
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 2.dp)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                items(
                    items = groups,
                    key = { g -> "${g.source}-${g.postId}" }
                ) { g ->
                    MyReviewComment(
                        group = g,
                        myId = myId,
                        myNickname = me?.nickname
                    )
                }
            }
        }
    }
}