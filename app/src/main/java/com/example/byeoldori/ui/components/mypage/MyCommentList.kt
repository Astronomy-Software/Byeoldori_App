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
    vm: CommunityViewModel = hiltViewModel(),
    eduVm: EducationViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        userVm.getMyProfile()
        reviewVm.loadPosts()
        eduVm.loadPosts()
        vm.loadPosts()
    }

    val me = userVm.userProfile.collectAsState().value
    val myId = me?.id ?: -1L
    val reviewState by reviewVm.postsState.collectAsState()
    val eduState by eduVm.postsState.collectAsState()
    val vmState by vm.postsState.collectAsState()

    var groups by remember { mutableStateOf<List<MyCommentGroup>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun <T> buildGroups(
        source: CommentSourceType,
        posts: List<T>,
        id: (T) -> Long,
        title: (T) -> String,
        authorName: (T) -> String,
        createdAt: (T) -> String,
    ): List<MyCommentGroup> = coroutineScope {
        posts.map { p->
            async {
                val postId = id(p)
                val all = commentsVm.AllMyComments(postId)
                val mine = all.filter { it.authorId == myId && !it.deleted }

                if(mine.isEmpty()) null else MyCommentGroup(
                    source = source,
                    postId = postId,
                    postTitle = title(p),
                    postAuthorName = authorName(p),
                    postCreatedAt = createdAt(p),
                    myComments = mine.map {
                        MyCommentUi(
                            source = source,
                            postId = postId,
                            postTitle = title(p),
                            postAuthorName = authorName(p),
                            commentId = it.id,
                            content = it.content.orEmpty(),
                            createdAt = it.createdAt
                        )
                    }
                )
            }
        }.mapNotNull { it.await() }
    }

    LaunchedEffect(myId,reviewState) {
        if (myId <= 0) return@LaunchedEffect
        loading = true; error = null
        runCatching {
            val reviews = (reviewState as? UiState.Success)?.data.orEmpty()
            val boards = (vmState as? UiState.Success)?.data.orEmpty()
            val educations = (eduState as? UiState.Success)?.data.orEmpty()

            val reviewGroup = buildGroups(
                source = CommentSourceType.REVIEW,
                posts = reviews,
                id = { it.id },
                title = { it.title },
                authorName = { it.authorNickname ?: "익명" },
                createdAt = { it.createdAt }
            )

            val boardGroup = buildGroups(
                source = CommentSourceType.FREE,
                posts = boards,
                id = { it.id },
                title = { it.title },
                authorName = { it.authorNickname ?: "익명" },
                createdAt = { it.createdAt }
            )

            val eduGroup = buildGroups(
                source = CommentSourceType.EDUCATION,
                posts = educations,
                id = { it.id },
                title = { it.title },
                authorName = { it.authorNickname ?: "익명" },
                createdAt = { it.createdAt }
            )

            (reviewGroup + boardGroup + eduGroup)
                .sortedByDescending { it.postCreatedAt }
        }.onSuccess { groups = it; loading = false }
            .onFailure { e -> error = e.message ?: "댓글 로드 실패"; loading = false }
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
                    MyComment(
                        group = g,
                        myId = myId,
                        myNickname = me?.nickname
                    )
                }
            }
        }
    }
}