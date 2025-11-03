package com.example.byeoldori.ui.components.community.freeboard

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.data.model.dto.FreePostResponse
import com.example.byeoldori.data.model.dto.PostDetailResponse
import com.example.byeoldori.domain.Community.*
import com.example.byeoldori.domain.Content
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.mapper.toUi
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.*
import com.example.byeoldori.viewmodel.Community.*
import kotlinx.coroutines.launch

private fun mergeApiIntoFree(api: PostDetailResponse, base: FreePost): FreePost {
    val textItem = Content.Text(api.content)
    val photos = (api.images).map { Content.Image.Url(it) }
    return base.copy(
        title = api.title,
        createdAt = api.createdAt,
        likeCount = api.likeCount,
        commentCount = api.commentCount,
        liked = api.liked,
        contentItems = listOf(textItem) + photos
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeBoardDetail (
    post: FreePost, //더미 Post
    onBack: () -> Unit,
    onShare: () -> Unit = {},
    apiPost: PostDetailResponse? = null, //api에서 받은 Post,
    vm: CommunityViewModel? = null,
    onSyncFreeLikeCount: (id: String, liked: Boolean, next: Int) -> Unit = { _, _, _ -> },
    onEdit: Boolean = true,
    onDelete: (postId: Long) -> Unit = {},
    onEditPost: (FreePost) -> Unit = {}
) {
    var input by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var editingTarget by remember { mutableStateOf<ReviewComment?>(null) }
    var requestKeyboard by remember { mutableStateOf(false) }

    var parent by remember { mutableStateOf<ReviewComment?>(null) }
    val commentsVm: CommentsViewModel = hiltViewModel() //화면에 연결된 CommentsViewModel 인스턴스 주입
    LaunchedEffect(post.id) {
        commentsVm.start(post.id)
    }
    val commentsState by commentsVm.comments.collectAsState()

    val userVm: UserViewModel = hiltViewModel()
    LaunchedEffect(Unit) { userVm.getMyProfile() }
    val me = userVm.userProfile.collectAsState().value
    val currentUserId = me?.id
    val currentUserNickname = me?.nickname

    //상태 변화 즉시 리스트 갱신
    val commentList: List<ReviewComment> = (commentsState as? UiState.Success)?.data ?: emptyList()
    val nonDeletedCount = remember(commentList) { commentList.count { !it.deleted } }

    LaunchedEffect(post.id, nonDeletedCount) {
        vm?.overrideFreeCommentCount(post.id, nonDeletedCount)
    }
    val commentCountUi = nonDeletedCount.takeIf { it > 0 }
        ?: (apiPost?.commentCount ?: post.commentCount)

    var moreMenu by remember { mutableStateOf(false) }
    var showDeleted by remember { mutableStateOf(false) }

    var deleteTarget by remember { mutableStateOf<ReviewComment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isOwner = remember(currentUserId, currentUserNickname, apiPost, post) {
        when {
            apiPost?.authorId != null && currentUserId != null -> apiPost.authorId == currentUserId
            else -> false
        }
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var likedCommentIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val currentDetail = (vm?.postDetail?.collectAsState()?.value as? UiState.Success)?.data
    val likeCount = currentDetail?.likeCount ?: post.likeCount
    val liked = currentDetail?.liked ?: post.liked

    LaunchedEffect(commentList) { likedCommentIds = commentList.filter { it.liked }.map { it.id }.toSet() }

    LaunchedEffect(requestKeyboard) {
        if (requestKeyboard) {
            focusRequester.requestFocus()
            keyboardController?.show()
            requestKeyboard = false  // 한 번만 실행
        }
    }

    LaunchedEffect(post.id) { commentsVm.start(post.id) }
    LaunchedEffect(currentUserId, currentUserNickname) { Log.d("CommentCheck", "FreeBoardDetail 진입: meId=$currentUserId, meNick=$currentUserNickname") }

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
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
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
                                contentDescription = "수정",
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
                                            vm?.resetFreeWriteStates()
                                            val editable = apiPost?.let { mergeApiIntoFree(it, post) } ?: post
                                            onEditPost(editable)
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
                Divider(color = Color.LightGray.copy(alpha = 0.4f), thickness = 1.dp)
            }
        },
        bottomBar = {
            if (editingTarget == null) { //댓글 수정 모드가 아닐때만 표시
                CommentInput(
                    text = input,
                    onTextChange = { input = it },
                    onSend = { raw ->
                        val t = raw.trim()
                        if (t.isEmpty()) return@CommentInput

                        if (editingTarget != null) {   //수정 모드
                            val targetId = editingTarget!!.id
                            val postId = post.id.toLongOrNull()
                            if (targetId != null && postId != null) {
                                commentsVm.update(
                                    postId = postId,
                                    commentId = targetId,
                                    content = t
                                ) {
                                    // 성공 콜백: 입력창/모드 초기화
                                    input = ""
                                    editingTarget = null
                                    parent = null
                                }
                            }
                        } else {   //일반 댓글 작성
                            val parentIdStr = parent?.id.toString()
                            commentsVm.submit(content = t, parentId = parentIdStr) {
                                input = ""
                                parent = null
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .advancedImePadding() // 키보드 위에 바가 딱 붙도록
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
                        val authorName = vm?.findNicknameByAuthorId(apiPost?.authorId ?: -1L) ?: "익명"
                        Text(text = authorName, fontSize = 17.sp, color = TextHighlight)
                        Spacer(Modifier.height(4.dp))
                        Text( //작성일
                            text = apiPost?.createdAt?.toShortDate() ?: post.createdAt.toShortDate(),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                            fontSize = 17.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                val domainItems: List<Content> =
                    if (apiPost != null) {
                        buildList {
                            val text = apiPost.content
                            if (text.isNotBlank()) add(Content.Text(text))
                            (apiPost.images).forEach { add(Content.Image.Url(it)) }
                        }
                    } else {
                        post.contentItems
                    }

                val hasImages = hasHttpImage(domainItems)
                val uiItems = domainItems.toUi()
                if(hasImages) {
                    ContentInput( //내용 입력(텍스트 + 이미지)
                        items = if (apiPost != null) {
                            val text = apiPost.content
                            val textItem = if (text.isNotBlank()) {
                                listOf(EditorItem.Paragraph(value = TextFieldValue(text)))
                            } else emptyList()

                            val photoItems = apiPost.images
                                .map { url -> EditorItem.Photo(model = url) }
                            textItem + photoItems   // ← 텍스트 + 이미지 함께 렌더링
                        } else {
                            post.contentItems.toUi()
                        },
                        onItemsChange = {},
                        onPickImages = {},
                        onCheck = {},
                        onChecklist = {},
                        readOnly = true
                    )
                } else {
                    ReadOnlyParagraphs(uiItems)
                }
                Spacer(Modifier.height(16.dp))

                //좋아요 + 댓글바
                LikeCommentBar(
                    likeCount = likeCount,
                    liked = liked,
                    onToggle = { post.id.toLongOrNull()?.let { id -> vm?.toggleLike(id) } },
                    onSyncLikeCount = {},
                    commentCount = commentCountUi
                )

                //댓글 + 대댓글
                CommentList(
                    postId = post.id.toLong(),
                    currentUserId = currentUserId,
                    currentUserNickname = currentUserNickname,
                    comments = commentList,
                    onLike = { tapped ->
                        commentsVm.toggleLike(tapped.id)
                    },
                    onLikedChange = {},
                    onReply = { target ->
                        parent = target
                        requestKeyboard = true
                    },
                    onEdit = { target ->
                        editingTarget = target
                        input = ""
                        parent = null
                    },
                    liked = likedCommentIds,
                    onDelete = { target ->
                        deleteTarget = target
                        showDeleteDialog = true
                    },
                    editingId = editingTarget?.id,
                    onSubmitEditInline = { target, newText ->
                        val postId = post.id.toLongOrNull()
                        val cid = target.id
                        if (postId != null && newText.isNotBlank()) {
                            commentsVm.update(
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
            title = { Text("게시글 삭제",color = Color.Black) },
            text = { Text("정말로 이 게시글을 삭제하시겠어요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleted = false
                        moreMenu = false
                        onDelete(post.id.toLong())
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
            text = { Text("이 댓글을 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cid = deleteTarget!!.id
                        commentsVm.delete(cid) {
                            // 성공 시 닫기
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

@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 420, heightDp = 1000)
@Composable
private fun Preview_FreeBoardDetail() {
    val sample = remember { dummyFreePosts.first() }
    FreeBoardDetail(
        post = sample,
        onBack = {},
        onShare = {},
        apiPost = null,
        vm = null
    )
}