package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.data.model.dto.EducationResponse
import com.example.byeoldori.domain.Community.EduProgram
import com.example.byeoldori.ui.components.community.program.*
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.*
import com.example.byeoldori.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProgramList(
    onBack: () -> Unit = {},
    eduVm: EducationViewModel = hiltViewModel(),
    vm: CommunityViewModel = hiltViewModel(),
    userVm: UserViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        userVm.getMyProfile()
        eduVm.loadPosts()
    }
    val me = userVm.userProfile.collectAsState().value
    val myId = me?.id

    var selectedProgram by remember { mutableStateOf<EduProgram?>(null) }
    var editingProgram by remember { mutableStateOf<EduProgram?>(null) }

    val postState by eduVm.postsState.collectAsState()
    val allPrograms = when(postState) {
        is UiState.Success -> (postState as UiState.Success<List<EducationResponse>>)
            .data.map { it.toEduProgram() }
        else -> emptyList()
    }

    val commentsVm: CommentsViewModel = hiltViewModel()
    val counts by commentsVm.commentCounts.collectAsState()

    val myPrograms = remember(allPrograms,myId) {
        if(myId == null) emptyList() else allPrograms.filter { it.authorId == myId }
    }

    val myProgramsUi = remember(myPrograms, counts) {
        myPrograms.map { p ->
            p.copy(commentCount = counts[p.id] ?: p.commentCount)
        }
    }

    Background(modifier = Modifier.fillMaxSize()) {
        when {
            editingProgram != null -> {
                /**이 부분도 추후 추가 예정*/
            }
            selectedProgram != null -> {
                val program = selectedProgram!!
                LaunchedEffect(program.id) {
                    program.id.toLongOrNull()?.let { eduVm.loadEducationDetail(it) }
                }

                EduProgramDetail(
                    program = program,
                    onBack = { selectedProgram = null },
                    onEditProgram = { editable ->
                        editingProgram = editable
                    },
                    onDelete = { id ->
                        vm.deletePost(id) {
                            selectedProgram = null
                            eduVm.loadPosts()
                            vm.loadPosts()
                        }
                    },
                    vm = eduVm
                )
            }
            else -> {
                Column(Modifier.fillMaxSize()) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "내가 작성한 교육 프로그램",
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

                    if (myPrograms.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("내가 작성한 교육 프로그램이 없습니다.", color = TextDisabled)
                        }
                    } else {
                        EduProgramGrid(
                            programs = myProgramsUi,
                            onClickProgram = { myPrograms ->
                                selectedProgram = myPrograms
                            },
                            onToggleLike = { id ->
                                eduVm.toggleLike(id.toLong()) {
                                    eduVm.loadPosts()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}