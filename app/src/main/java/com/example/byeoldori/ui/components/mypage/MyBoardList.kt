package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.data.model.dto.FreePostResponse
import com.example.byeoldori.domain.Community.FreePost
import com.example.byeoldori.ui.components.community.freeboard.*
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.CommunityViewModel
import com.example.byeoldori.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBoardList(
    onBack: () -> Unit = {},
    vm: CommunityViewModel = hiltViewModel(),
    userVm: UserViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        userVm.getMyProfile()
        vm.loadPosts()
    }
    val me = userVm.userProfile.collectAsState().value
    val myId = me?.id

    val postState by vm.postsState.collectAsState()
    val allPosts = when(postState) {
        is UiState.Success -> (postState as UiState.Success<List<FreePostResponse>>)
            .data.map { it.toFreePost() }
        else -> emptyList()
    }

    val myPosts = remember(allPosts,myId) {
        if(myId == null) emptyList() else allPosts.filter { it.authorId == myId }
    }

    var selectedFree by remember { mutableStateOf<FreePost?>(null) }
    var editingFree    by remember { mutableStateOf<FreePost?>(null) }

    val freeDetailState by vm.postDetail.collectAsState()

    Background(modifier = Modifier.fillMaxSize()) {
        when {
            editingFree != null -> {
                val draft = editingFree!!
                FreeBoardWriteForm(
                    onCancel = { editingFree = null; selectedFree = null },
                    onSubmit = {
                        editingFree = null
                        selectedFree = null
                        vm.loadPosts()
                    },
                    onTempSave = {},
                    onMore = {},
                    onSubmitPost = {},
                    initialPost = draft,
                    onClose = { editingFree = null; selectedFree = null },
                    vm = vm
                )
            }

            selectedFree != null -> {
                val free = selectedFree!!
                val apiPost = (freeDetailState as? UiState.Success)?.data

                LaunchedEffect(free.id) {
                    free.id.toLongOrNull()?.let { vm.loadPostDetail(it) }
                }

                FreeBoardDetail(
                    post = free,
                    apiPost = apiPost,
                    vm = vm,
                    onBack = { selectedFree = null },
                    onEditPost = { editable ->
                        editingFree = editable
                    },
                    onDelete = { id ->
                        vm.deletePost(id) {
                            selectedFree = null
                            vm.loadPosts()
                        }
                    }
                )
            }

            else -> {
                Column(Modifier.fillMaxSize()) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "내가 작성한 자유게시글",
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

                    if (myPosts.isEmpty()) {
                        Box(Modifier.fillMaxSize()) {
                            Text("내가 작성한 게시글이 없습니다.", color = TextDisabled)
                        }
                    } else {
                        FreeGrid(
                            posts = myPosts,
                            onClick = { post ->
                                selectedFree = post
                            },
                            onToggle = { id -> vm.toggleLike(id.toLong()) { vm.loadPosts() } }
                        )
                    }
                }
            }
        }
    }
}