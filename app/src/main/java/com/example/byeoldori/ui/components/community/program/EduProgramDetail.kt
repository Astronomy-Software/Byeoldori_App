    package com.example.byeoldori.ui.components.community.program

    import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import com.example.byeoldori.R
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.data.model.dto.EducationDetailResponse
import com.example.byeoldori.data.model.dto.EducationResponse
import com.example.byeoldori.domain.Community.EduProgram
import com.example.byeoldori.domain.Community.ReviewComment
import com.example.byeoldori.eduprogram.EduViewModel
import com.example.byeoldori.ui.components.community.CommentInput
import com.example.byeoldori.ui.components.community.CommentList
import com.example.byeoldori.ui.components.community.ContentInput
import com.example.byeoldori.ui.components.community.EditorItem
import com.example.byeoldori.ui.components.community.LikeCommentBar
import com.example.byeoldori.ui.components.community.freeboard.formatCreatedAt
import com.example.byeoldori.ui.components.community.review.advancedImePadding
import com.example.byeoldori.ui.components.community.toShortDate
import com.example.byeoldori.ui.mapper.toUi
import com.example.byeoldori.ui.theme.Purple600
import com.example.byeoldori.ui.theme.Purple700
import com.example.byeoldori.ui.theme.TextDisabled
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.viewmodel.Community.CommentsViewModel
import com.example.byeoldori.viewmodel.Community.EducationViewModel
import com.example.byeoldori.viewmodel.UiState
import com.example.byeoldori.viewmodel.dummyPrograms
import kotlinx.coroutines.launch

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EduProgramDetail(
        program: EduProgram,
        onBack: () -> Unit,
        onShare: () -> Unit = {},
        currentUser: String? = null,
        vm: EducationViewModel? = null,
        onStartProgram: () -> Unit = {},
        onEdit: Boolean = true,
        onDelete: (programId: String) -> Unit = {},
        onEditProgram: (EduProgram) -> Unit = {}
    ) {
        val userVm: UserViewModel = hiltViewModel()
        val activity = LocalActivity.current as? ViewModelStoreOwner
        val eduVm: EduViewModel = if (activity != null) {
            hiltViewModel(activity)
        } else {
            hiltViewModel()
        }

        LaunchedEffect(Unit) { userVm.getMyProfile() }
        val me = userVm.userProfile.collectAsState().value
        val myId = me?.id
        var input by rememberSaveable { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }
        var editingTarget by remember { mutableStateOf<ReviewComment?>(null) }
        var requestKeyboard by remember { mutableStateOf(false) }
        var parent by remember { mutableStateOf<ReviewComment?>(null) }

        val detailState by (vm?.detail?.collectAsState()
            ?: remember { mutableStateOf<UiState<EducationDetailResponse>>(UiState.Idle) })

        val postsState by (vm?.postsState?.collectAsState()
            ?: remember { mutableStateOf<UiState<List<EducationResponse>>>(UiState.Idle) })

        val apiDetail = (detailState as? UiState.Success<EducationDetailResponse>)?.data
        val apiPost = (postsState as? UiState.Success<List<EducationResponse>>)
            ?.data?.firstOrNull { it.id.toString() == program.id }

        var likeCount by rememberSaveable(program.id) {
            mutableStateOf(apiPost?.likeCount ?: program.likeCount)
        }
        var liked by rememberSaveable(program.id) {
            mutableStateOf(apiPost?.liked ?: program.liked)
        }

        val createdText = apiPost?.createdAt
            ?.let { formatCreatedAt(it) }
            ?.takeIf { it.isNotBlank() }
            ?: program.createdAt.toShortDate()

        val commentsVm: CommentsViewModel = hiltViewModel()
        val commentsState by commentsVm.comments.collectAsState()
        val commentCounts by commentsVm.commentCounts.collectAsState()

        val commentCountUi = commentCounts[program.id] ?: when (val s = commentsState) {
            is UiState.Success -> s.data.size
            else -> 0
        }

        val commentList: List<ReviewComment> =
            (commentsState as? UiState.Success)?.data ?: emptyList()

        val myNick: String? = if (myId == null) currentUser else null

        val likedCommentIds by remember(commentList) {
            mutableStateOf(commentList.filter { it.liked }.map { it.id }.toSet())
        }

        var moreMenu by remember { mutableStateOf(false) }
        var showDeleted by remember { mutableStateOf(false) }
        var deleteTarget by remember { mutableStateOf<ReviewComment?>(null) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        val postAuthorId: Long? = apiPost?.authorId ?: apiDetail?.authorId
        val isOwner = remember(myId, postAuthorId) {
            myId != null && postAuthorId != null && myId == postAuthorId
        }

        LaunchedEffect(requestKeyboard) {
            if (requestKeyboard) {
                focusRequester.requestFocus()
                keyboardController?.show()
                requestKeyboard = false  // 한 번만 실행
            }
        }

        LaunchedEffect(program.id) {
            vm?.loadEducationDetail(program.id.toLong())
        }

        LaunchedEffect(program.id) {
            commentsVm.start(program.id)
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
                                    if (onEdit && isOwner) {
                                        DropdownMenuItem(
                                            text = { Text("수정", color = Color.Black) },
                                            onClick = {
                                                moreMenu = false
                                                onEditProgram(program)
                                            }
                                        )
                                        Divider(
                                            color = Color.Black.copy(alpha = 0.6f),
                                            thickness = 1.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        DropdownMenuItem(
                                            text = { Text("삭제", color = Color.Black) },
                                            onClick = {
                                                moreMenu = false
                                                showDeleted = true
                                            }
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
                if (editingTarget == null) {
                    CommentInput(
                        text = input,
                        onTextChange = { input = it },
                        onSend = { raw ->
                            val t = raw.trim()
                            if (t.isEmpty()) return@CommentInput

                            if (editingTarget != null) {   //수정 모드
                                val targetId = editingTarget!!.id.toLongOrNull()
                                val postId = program.id.toLongOrNull()
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
                                val parentIdStr = parent?.id
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
                    Text(text = apiPost?.title ?: program.title, fontSize = 24.sp, color = TextHighlight) //제목
                    Spacer(Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val profilePainter = program.profile
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
                            Text(text = apiPost?.authorNickname ?: program.author, fontSize = 17.sp, color = TextHighlight)
                            Spacer(Modifier.height(4.dp))
                            Text( //작성일
                                text =  createdText,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                                fontSize = 17.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(text = "관측 대상", style = MaterialTheme.typography.labelLarge.copy(color = TextDisabled))
                            Spacer(Modifier.height(6.dp))
                            Text(
                            text = apiDetail?.education?.target ?: program.target ?: "—",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                        }

                        Column(Modifier.weight(1f)) {
                            Text(text = "평점", style = MaterialTheme.typography.labelLarge.copy(color = TextDisabled))
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = (apiDetail?.education?.averageScore ?: program.averageScore ?: 0.0).toString(),
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    apiDetail?.education?.summary?.let { summary ->
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    ContentInput( //내용 입력(텍스트 + 이미지)
                        items = when {
                            apiDetail?.content != null -> listOf(
                                EditorItem.Paragraph(
                                    value = TextFieldValue(apiDetail.content)
                                )
                            )
                            else -> program.contentItems.toUi()
                        },
                        onItemsChange = {},
                        onPickImages = {},
                        onCheck = {},
                        onChecklist = {},
                        readOnly = true
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                val id = program.id.toLongOrNull() ?: return@Button
                                val url = apiDetail?.education?.contentUrl

                                if (url.isNullOrEmpty()) {
                                    scope.launch {
                                        snackbar.showSnackbar("교육 콘텐츠 URL이 존재하지 않습니다.")
                                    }
                                    return@Button
                                }
                                eduVm.openProgram(programId = id, url = url)
                            } ,
                            modifier = Modifier
                                .width(350.dp)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Purple700)
                        ) {
                            Text(
                                text = "지금 바로 교육 시청하기",
                                color = TextHighlight,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    //좋아요 + 댓글바
                    LikeCommentBar(
                        likeCount = likeCount,
                        liked = liked,
                        onToggle = {
                            program.id.toLongOrNull()?.let { pid ->
                                vm?.toggleLike(pid) { res ->
                                    liked = res.liked// 상세 즉시 반영
                                    likeCount = res.likes.toInt()
                                }
                            }
                        },
                        onSyncLikeCount = {},
                        commentCount = commentCountUi
                    )
                    //댓글 + 대댓글
                    CommentList(
                        postId = program.id,
                        currentUserId = myId,
                        currentUserNickname = myNick,
                        comments = commentList,
                        liked = likedCommentIds,
                        onLikedChange = {},
                        onLike = { tapped ->
                            tapped.id.toLongOrNull()?.let { cid ->
                                commentsVm.toggleLike(cid)
                            }
                        },
                        onReply = { target ->
                            parent = target
                            requestKeyboard = true
                        },
                        onEdit = { target ->
                            editingTarget = target
                            requestKeyboard = true
                        },
                        onDelete = { target ->
                            deleteTarget = target
                            showDeleteDialog = true
                        },
                        editingId = editingTarget?.id,
                        onSubmitEditInline = { target, newText ->
                            val postId = program.id.toLongOrNull()
                            val cid = target.id.toLongOrNull()
                            if (postId != null && cid != null && newText.isNotBlank()) {
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
                title = { Text("교욱 프로그램 삭제",color = Color.Black) },
                text = { Text("정말로 이 교육 프로그램을 삭제하시겠어요?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleted = false
                            moreMenu = false
                            onDelete(program.id)
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
                                commentsVm.delete(cid) {
                                    // 성공 시 닫고, 목록/카운트 갱신 트리거
                                    showDeleteDialog = false
                                    deleteTarget = null
                                   // vm?.loadPosts()
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

    @Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 500, heightDp = 1000)
    @Composable
    private fun Preview_EduProgramDetail() {
        val sample = remember { dummyPrograms.first() }
        EduProgramDetail(
            program = sample,
            onBack = {},
            onShare = {},
            currentUser = "astro_user",
            onStartProgram = {},
            vm = null
        )
    }