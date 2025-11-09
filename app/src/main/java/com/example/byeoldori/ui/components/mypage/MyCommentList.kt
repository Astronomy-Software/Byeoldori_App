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
import com.example.byeoldori.data.model.dto.ReviewDetailResponse
import com.example.byeoldori.data.model.dto.ReviewResponse
import com.example.byeoldori.ui.components.community.freeboard.FreeBoardDetail
import com.example.byeoldori.ui.components.community.freeboard.toFreePost
import com.example.byeoldori.ui.components.community.program.EduProgramDetail
import com.example.byeoldori.ui.components.community.program.toEduProgram
import com.example.byeoldori.ui.components.community.review.ReviewDetail
import com.example.byeoldori.ui.components.community.review.toReview
import org.w3c.dom.Comment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCommentList(
    onBack: () -> Unit = {},
    reviewVm: ReviewViewModel = hiltViewModel(),
    userVm: UserViewModel = hiltViewModel(),
    commentsVm: CommentsViewModel = hiltViewModel(),
    vm: CommunityViewModel = hiltViewModel(),
    eduVm: EducationViewModel = hiltViewModel(),
    onOpenReviewDetail: (postId: Long) -> Unit = {},
    onOpenFreeDetail: (postId: Long) -> Unit = {},
    onOpenEducationDetail: (postId: Long) -> Unit = {}
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

    var selected by remember { mutableStateOf<Pair<CommentSourceType, Long>?>(null) }

    suspend fun <T> buildGroups(
        source: CommentSourceType,
        posts: List<T>,
        id: (T) -> Long,
        title: (T) -> String,
        authorName: (T) -> String,
        authorProfileUrl: (T) -> String?,
        createdAt: (T) -> String,
    ): List<MyCommentGroup> = coroutineScope {
        posts.map { p->
            async {
                val postId = id(p)
                val all = commentsVm.AllMyComments(postId) //해당 게시글에 달린 모든 댓글 수집
                val mine = all.filter { it.authorId == myId && !it.deleted } //내가 쓴 댓글만 필터링

                //내가 쓴 댓글이 존재하면 그룹 객체 생성
                if(mine.isEmpty()) null else MyCommentGroup(
                    source = source,
                    postId = postId,
                    postTitle = title(p),
                    postAuthorName = authorName(p),
                    postAuthorProfileUrl = authorProfileUrl(p),
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
        }.mapNotNull { it.await() } //async로 동시에 실행
    }

    //게시글 상태 변경되면 다시 로드
    LaunchedEffect(myId,reviewState,vmState,eduState) {
        if (myId <= 0) return@LaunchedEffect
        loading = true; error = null
        runCatching {
            val reviews = (reviewState as? UiState.Success)?.data.orEmpty()
            val boards = (vmState as? UiState.Success)?.data.orEmpty()
            val educations = (eduState as? UiState.Success)?.data.orEmpty()

            val reviewGroup = buildGroups( //관측 후기에서 내가 쓴 댓글들
                source = CommentSourceType.REVIEW,
                posts = reviews,
                id = { it.id },
                title = { it.title },
                authorName = { it.authorNickname ?: "익명" },
                authorProfileUrl = { it.authorProfileImageUrl },
                createdAt = { it.createdAt }
            )

            val boardGroup = buildGroups(
                source = CommentSourceType.FREE,
                posts = boards,
                id = { it.id },
                title = { it.title },
                authorName = { it.authorNickname ?: "익명" },
                authorProfileUrl = { it.authorProfileImageUrl },
                createdAt = { it.createdAt }
            )

            val eduGroup = buildGroups(
                source = CommentSourceType.EDUCATION,
                posts = educations,
                id = { it.id },
                title = { it.title },
                authorName = { it.authorNickname ?: "익명" },
                authorProfileUrl = { it.authorProfileImageUrl },
                createdAt = { it.createdAt }
            )

            (reviewGroup + boardGroup + eduGroup)
                .sortedByDescending { it.postCreatedAt }
        }.onSuccess { groups = it; loading = false }
            .onFailure { e -> error = e.message ?: "댓글 로드 실패"; loading = false }
    }

    selected?.let { (src, postId) ->
        when(src) {
            CommentSourceType.FREE -> {
                val boards = (vmState as? UiState.Success)?.data.orEmpty()
                val post = boards.firstOrNull { it.id == postId }?.toFreePost()

                LaunchedEffect(postId) { vm.loadPostDetail(postId) }

                val postDetailUi by vm.postDetail.collectAsState()
                val apiPost = (postDetailUi as? UiState.Success)?.data

                if(post != null) {
                    FreeBoardDetail(
                        post = post,
                        apiPost = apiPost,
                        onBack = { selected = null },
                        vm = vm,
                        onEdit = false,
                        onDelete = { id ->
                            vm.deletePost(id) {
                                // 삭제 후 목록 갱신 및 복귀
                                vm.loadPosts()
                                selected = null
                            }
                        }
                    )
                }
            }
            CommentSourceType.REVIEW -> {
                val reviewDtos = (reviewState as? UiState.Success)?.data.orEmpty()
                val apiPost: ReviewResponse? = reviewDtos.firstOrNull { it.id == postId }

                LaunchedEffect(postId) { reviewVm.loadReviewDetail(postId) }
                val detailUi by reviewVm.detailUi.collectAsState()
                val latestReview = (detailUi as? UiState.Success)?.data

                val detailDto by reviewVm.detail.collectAsState()                  // UiState<ReviewDetailResponse>
                val apiDetail = (detailDto as? UiState.Success)?.data              // ReviewDetailResponse?

                val reviewForScreen = latestReview ?: apiPost?.toReview()

                if(reviewForScreen != null) {
                    ReviewDetail(
                        review = reviewForScreen,
                        onBack = { selected = null },
                        currentUser = (me?.id?.toString() ?: me?.nickname ?: "익명"), // ReviewDetail 시그니처 맞춤
                        currentUserId = me?.id,
                        onSyncReviewLikeCount = { _, _, _ -> },
                        onEdit = false,
                        apiDetail = apiDetail,
                        apiPost = apiPost,
                        vm = reviewVm,
                    )
                }
            }
            CommentSourceType.EDUCATION -> {
                val educations = (eduState as? UiState.Success)?.data.orEmpty()
                val education = educations.firstOrNull { it.id == postId}?.toEduProgram()
                if(education != null) {
                    EduProgramDetail(
                        program = education,
                        onBack = { selected = null },
                        onEdit = false,
                        vm = eduVm
                    )
                }
            }
        }
        return
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
                        myNickname = me?.nickname,
                        myProfileUrl = me?.profileImageUrl,
                        onOpenDetail = { selected = g.source to g.postId }
                    )
                }
            }
        }
    }
}