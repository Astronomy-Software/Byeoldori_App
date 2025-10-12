package com.example.byeoldori.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.domain.Community.EduProgram
import com.example.byeoldori.domain.Community.FreePost
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.components.community.HomeSection
import com.example.byeoldori.ui.components.community.freeboard.*
import com.example.byeoldori.ui.components.community.program.*
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.components.observatory.CurrentWeatherSection
import com.example.byeoldori.ui.home.GetLocation
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.viewmodel.Community.*
import com.example.byeoldori.viewmodel.Observatory.*
import com.example.byeoldori.viewmodel.UiState

@Composable
fun HomeScreen(
    vm: NaverMapViewModel = hiltViewModel(),
    reviewVm: ReviewViewModel = hiltViewModel(),
    eduVm: EducationViewModel = hiltViewModel(),
    communityVm: CommunityViewModel = hiltViewModel()
) {

    val locationState = GetLocation(vm)
    var suitability by remember { mutableStateOf<Int?>(null) }

    // 각 VM 상태 수집 → UI 모델로 변환
    val reviewList = when (val s = reviewVm.postsState.collectAsState().value) {
        is UiState.Success -> s.data.map { it.toReview() }
        else -> emptyList()
    }
    val eduList = when (val s = eduVm.postsState.collectAsState().value) {
        is UiState.Success -> s.data.map { it.toEduProgram() }
        else -> emptyList()
    }
    val freeList = when (val s = communityVm.postsState.collectAsState().value) {
        is UiState.Success -> s.data.map { it.toFreePost() }
        else -> emptyList()
    }

    var selectedReview by remember { mutableStateOf<Review?>(null) }
    var selectedEduProgram by remember { mutableStateOf<EduProgram?>(null) }
    var selectedPost by remember { mutableStateOf<FreePost?>(null) }

    if(selectedReview != null) {
        val detailState = reviewVm.detail.collectAsState().value

        LaunchedEffect(selectedReview!!.id) {
            reviewVm.resetDetail()
            val reviewIdLong = selectedReview!!.id.filter(Char::isDigit).toLongOrNull()
            if (reviewIdLong != null) {
                reviewVm.loadReviewDetail(reviewIdLong)
            }
        }

        when (detailState) {
            is UiState.Loading, UiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
               Text("관측지 리뷰를 불러오지 못했습니다.")
            }
            is UiState.Success -> {
                ReviewDetail(
                    review = selectedReview!!,
                    onBack = { selectedReview = null },
                    vm = reviewVm,
                    apiDetail = detailState.data,
                    apiPost = null,
                    currentUser = "헤이헤이",
                    onSyncReviewLikeCount = { _, _, _ -> }
                )
            }
        }
        return
    }

    if(selectedPost != null) {
        val freeDetailState = communityVm.postDetail.collectAsState().value

        LaunchedEffect(selectedPost!!.id) {
            communityVm.resetPostDetail()
            val postIdLong = selectedPost!!.id
                .filter(Char::isDigit)      // 예: "free:123" → "123"
                .toLongOrNull()

            if(postIdLong != null) {
                communityVm.loadPostDetail(postIdLong)
            }
        }
        when(freeDetailState) {
            is UiState.Loading, UiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Text("자유게시판 게시글을 불러오지 못했습니다.")
            }
            is UiState.Success -> {
                FreeBoardDetail(
                    post = selectedPost!!,
                    onBack = { selectedPost = null },
                    vm = communityVm,
                    apiPost = freeDetailState.data
                )
            }
        }
        return
    }

    if(selectedEduProgram != null) {
        EduProgramDetail(
            program = selectedEduProgram!!,
            onBack = { selectedEduProgram = null },
            vm = eduVm,
            currentUser = "헤이헤이",
            onStartProgram = { /*TODO*/ }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp) // 위/아래 여백만
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("홈화면이에요", fontSize = 28.sp, color = TextHighlight)
                Spacer(Modifier.height(16.dp))

                if (locationState.lat != null && locationState.lon != null) {
                    Text("위도(Lat): ${"%.5f".format(locationState.lat)}", fontSize = 16.sp, color = TextHighlight)
                    Text("경도(Lon): ${"%.5f".format(locationState.lon)}", fontSize = 16.sp, color = TextHighlight)
                }
                if (locationState.address.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("주소: ${locationState.address}", fontSize = 16.sp, color = TextHighlight)
                }
                Spacer(Modifier.height(24.dp))

                when {
                    locationState.isLoading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator()
                            Spacer(Modifier.width(12.dp))
                            Text("현재 위치를 확인하는 중..")
                        }
                    }
                    locationState.lat != null && locationState.lon != null -> {
                        CurrentWeatherSection(
                            lat = locationState.lat!!,
                            lon = locationState.lon!!,
                            onSuitabilityChange = { suitability = it }
                        )
                    }
                    else -> Text("현재 날씨 정보를 가져올 수 없습니다.")
                }
            }
        }

        item {
            HomeSection(
                recentReviews = reviewList.take(20),
                recentEduPrograms = eduList.take(20),
                popularFreePosts = freeList.sortedByDescending { it.likeCount }.take(20),
                onReviewClick = { review ->
                    reviewVm.selectPost(review.id)
                    selectedReview = review
                },
                onProgramClick = { program ->
                    eduVm.selectPost(program.id)
                    selectedEduProgram = program
                },
                onFreePostClick = { post ->
                    communityVm.resetPostDetail()
                    communityVm.selectPost(post.id)
                    selectedPost = post
                },
                onSyncReviewLikeCount = { _, _ -> },
                enableInternalScroll = false,
                internalPadding = 16.dp
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}