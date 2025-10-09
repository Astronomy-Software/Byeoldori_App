package com.example.byeoldori.ui.components.community.program

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
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.domain.Community.*
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.components.community.freeboard.formatCreatedAt
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.mapper.toUi
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduProgramDetail(
    program: EduProgram,
    onBack: () -> Unit,
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
    currentUser: String,
    //vm: EducationViewModel? = hiltViewModel(),
    vm: EducationViewModel? = null,
    onStartProgram: () -> Unit = {}
) {
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

    var likedCommentIds by rememberSaveable(program.id) {
        mutableStateOf<Set<String>>(emptySet())
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
                        val idx =  dummyProgramComments.indexOfFirst { it.id == target.id }
                        if (idx >= 0) {
                            dummyProgramComments[idx] = target.copy(content = t)
                        }
                        // 모드 종료 + 입력 비우기
                        editingTarget = null
                        input = ""
                        return@CommentInput
                    }
                    dummyProgramComments.add(
                        ReviewComment(
                            id = "pc${System.currentTimeMillis()}",
                            reviewId = program.id,
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
                    parent?.let { p ->
                        val idx = dummyProgramComments.indexOfFirst { it.id == p?.id }
                        if (idx >= 0) { //부모 댓글 찾으면
                            val cur = dummyProgramComments[idx]
                            val next = cur.copy(commentCount = cur.commentCount + 1)
                            dummyProgramComments[idx] = next
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
                Text(text = apiPost?.title ?: program.title, fontSize = 24.sp, color = TextHighlight) //제목
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically
                ) {
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
                            text =  formatCreatedAt(apiPost?.createdAt) ?: program.createdAt.toShortDate(),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                            fontSize = 17.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

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
                        onClick = onStartProgram,
                        modifier = Modifier.width(350.dp).height(50.dp),
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
                    key = likedKeyProgram(program.id),
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
                    onSyncLikeCount = { next ->
                        // 목록 원본 동기화(정렬/표시 일치)
                        val idx = dummyPrograms.indexOfFirst { it.id == program.id }
                        if (idx >= 0) dummyPrograms[idx] = dummyPrograms[idx].copy(likeCount = next)
                    },
                    commentCount = dummyProgramComments.count { it.reviewId == program.id }
                )
                //댓글 + 대댓글
                CommentList(
                    postId = program.id,
                    currentUser = currentUser,
                    comments = dummyProgramComments,
                    liked = likedCommentIds,
                    onLikedChange = { newLikedIds ->
                        likedCommentIds = newLikedIds
                        val base = LikeState.ids.filterNot { it.startsWith("programComment:") }.toSet()
                        val withComments = base + newLikedIds.map { likedKeyProgramComment(it) }
                        LikeState.ids = withComments
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
                        val idx = dummyProgramComments.indexOfFirst { it.id == del.id }
                        if (idx >= 0) dummyProgramComments.removeAt(idx)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 420, heightDp = 1000)
@Composable
private fun Preview_EduProgramDetail() {
    val sample = remember { dummyPrograms.first() }
    EduProgramDetail(
        program = sample,
        onBack = {},
        onShare = {},
        onMore = {},
        currentUser = "astro_user",
        onStartProgram = {},
        vm = null
    )
}