package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.domain.Community.*
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.components.community.freeboard.*
import com.example.byeoldori.ui.components.community.program.*
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.*
import com.example.byeoldori.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikeSection(
    onBack: () -> Unit = {},
    eduVm: EducationViewModel = hiltViewModel(),
    reviewVm: ReviewViewModel = hiltViewModel(),
    vm: CommunityViewModel = hiltViewModel()
) {
    val tabs = listOf("교육 프로그램", "관측 후기","자유게시판")
    var selectedTab by remember { mutableStateOf(0) }

    var selectedProgram by remember { mutableStateOf<EduProgram?>(null) }
    var selectedReview by remember { mutableStateOf<Review?>(null) }
    var selectedFree by remember { mutableStateOf<FreePost?>(null) }

    LaunchedEffect(Unit) {
        eduVm.loadPosts()
        reviewVm.loadPosts()
        vm.loadPosts()
    }

    val eduState by eduVm.postsState.collectAsState()
    val allEduPrograms = when(eduState) {
        is UiState.Success -> (eduState as UiState.Success<List<EducationResponse>>)
            .data.map { it.toEduProgram() }
        else -> emptyList()
    }
    val likedPrograms = allEduPrograms.filter { it.liked } //좋아요 누른 항목만 필터링

    val reviewState by reviewVm.postsState.collectAsState()
    val allReviews = when(reviewState) {
        is UiState.Success -> (reviewState as UiState.Success<List<ReviewResponse>>)
            .data.map { it.toReview() }
        else -> emptyList()
    }
    val likedReviews = remember(allReviews) {
        mutableStateListOf<Review>().apply { addAll(allReviews.filter { it.liked }) }
    }

    val freeState by vm.postsState.collectAsState()
    val allFreePosts = when(freeState) {
        is UiState.Success -> (freeState as UiState.Success<List<FreePostResponse>>)
            .data.map { it.toFreePost() }
        else -> emptyList()
    }
    val likedFreePosts = remember(allFreePosts) {
        mutableStateListOf<FreePost>().apply { addAll(allFreePosts.filter { it.liked }) }
    }

    Background(modifier = Modifier.fillMaxSize()) {
        when {
            selectedProgram != null -> {
                EduProgramDetail(
                    program = selectedProgram!!,
                    onBack = { selectedProgram = null }, //뒤로가기 시 목록으로 복귀
                    vm = eduVm
                )
            }
            selectedReview != null -> {
                val review = selectedReview!!
                ReviewDetail(
                    review = selectedReview!!,
                    currentUser = "me",
                    onBack = { selectedReview = null },
                    vm = reviewVm,
                    onSyncReviewLikeCount = { id, liked, next ->
                        val idx = likedReviews.indexOfFirst { it.id == id }
                        if (idx >= 0) {
                            likedReviews[idx] = likedReviews[idx].copy(liked = liked, likeCount = next)
                        }
                    }
                )
                LaunchedEffect(review.id) {
                    review.id.toLongOrNull()?.let { reviewVm.loadReviewDetail(it) }
                }
            }
            selectedFree != null -> {
                val free = selectedFree!!
                val freeDetail by vm.postDetail.collectAsState()
                val apiPost = (freeDetail as? UiState.Success)?.data

                FreeBoardDetail(
                    post = free,
                    apiPost = apiPost,
                    vm = vm,
                    onBack = { selectedFree = null }
                )
                LaunchedEffect(free.id) { free.id.toLongOrNull()?.let { vm.loadPostDetail(it) } }
            }

            else -> Column(Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "좋아요",
                            color = Color.White,
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

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = {}, //강조선 제거
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            modifier = Modifier
                                .height(40.dp)
                                .weight(1f),
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) TextHighlight else TextDisabled,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = MaterialTheme.typography.titleSmall.fontSize * 1.2f
                                    )
                                )
                            }
                        )
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 2.dp)

                when (selectedTab) {
                    0 -> EduProgramGrid(
                            programs = likedPrograms,
                            onClickProgram = { id -> //상세화면 이동
                                likedPrograms.find { it.id == id }?.let { selectedProgram = it }
                            },
                            onToggleLike = { id -> eduVm.toggleLike(id.toLong()) }
                    )
                    1 -> ReviewGrid(
                        reviews = likedReviews,
                        onClickReview = { selectedReview = it },
                        onToggleLike = { id ->
                            reviewVm.toggleLike(id.toLong()) {
                                reviewVm.loadPosts()
                            }
                        }
                    )
                    2 -> FreeGrid(
                        posts = likedFreePosts,
                        onClick = { selectedFree = it },
                        onToggle = { id ->
                            vm.toggleLike(id.toLong()) {
                                // 서버 응답 후 목록 새고침(간단 모드)
                                vm.loadPosts()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 400, heightDp = 800)
@Composable
private fun PreviewLikesScreen() {
    LikeSection()
}