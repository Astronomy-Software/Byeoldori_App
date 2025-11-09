package com.example.byeoldori.ui.screen.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.byeoldori.data.model.dto.PostDetailResponse
import com.example.byeoldori.domain.Community.*
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.components.community.HomeSection
import com.example.byeoldori.ui.components.community.freeboard.*
import com.example.byeoldori.ui.components.community.program.*
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.components.mypage.*
import com.example.byeoldori.ui.components.observatory.CurrentWeatherSection
import com.example.byeoldori.ui.home.GetLocation
import com.example.byeoldori.ui.screen.MyPage.parseDateTimeFlexible
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.*
import com.example.byeoldori.viewmodel.Observatory.*
import com.example.byeoldori.viewmodel.UiState
import androidx.compose.ui.graphics.Color
import java.time.*

@Composable
fun HomeScreen(
    vm: NaverMapViewModel = hiltViewModel(),
    reviewVm: ReviewViewModel = hiltViewModel(),
    eduVm: EducationViewModel = hiltViewModel(),
    communityVm: CommunityViewModel = hiltViewModel(),
    planVm: PlanViewModel = hiltViewModel(),
    onOpenSchedule: () -> Unit = {}
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

    var calYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var calSelected by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(calYearMonth) {
        planVm.loadMonthPlans(calYearMonth.year, calYearMonth.monthValue)
    }

    val monthUi by planVm.monthPlansState.collectAsStateWithLifecycle()
    val plans = when(val s = monthUi) {
        is UiState.Success -> s.data
        else -> emptyList()
    }

    var showPlanSheet by remember { mutableStateOf(false) }

    val plansForSelectedDay = remember(calSelected, plans) {
        plans.filter { p ->
            val s = runCatching { parseDateTimeFlexible(p.startAt).toLocalDate() }.getOrNull()
            val e = runCatching { parseDateTimeFlexible(p.endAt).toLocalDate() }.getOrNull()
            if (s == null || e == null) return@filter false
            !calSelected.isBefore(s) && !calSelected.isAfter(e)
        }
    }

    val singleBadges = remember(calYearMonth, plans) {
        val today = LocalDate.now()
        buildMap<LocalDate, Color> {
            plans.forEach { p ->
                val s = runCatching { parseDateTimeFlexible(p.startAt).toLocalDate() }.getOrNull()
                val e = runCatching { parseDateTimeFlexible(p.endAt).toLocalDate() }.getOrNull()
                if (s != null && e != null && s == e && YearMonth.from(s) == calYearMonth) {
                    val isCompleted = (p.status?.name == "COMPLETED")
                    val color = when {
                        s.isBefore(today) && !isCompleted -> ErrorRed     // 과거 + 미작성
                        s.isBefore(today) -> SuccessGreen                  // 과거 + 완료
                        else -> WarningYellow                              // 미래
                    }
                    // 같은 날짜에 여러 일정이 있으면 우선순위: 빨강 > 초록 > 노랑
                    val prev = this[s]
                    this[s] = when {
                        prev == ErrorRed || color == ErrorRed -> ErrorRed
                        prev == SuccessGreen || color == SuccessGreen -> SuccessGreen
                        else -> WarningYellow
                    }
                }
            }
        }
    }

    //자정을 넘길 때
    val ranges = remember(calYearMonth, plans) {
        val today = LocalDate.now()
        plans.mapNotNull { p ->
            val s = runCatching { parseDateTimeFlexible(p.startAt).toLocalDate() }.getOrNull()
            val e = runCatching { parseDateTimeFlexible(p.endAt).toLocalDate() }.getOrNull()
            if (s != null && e != null && s != e) {
                val isCompleted = (p.status?.name == "COMPLETED")
                val color = when {
                    e.isBefore(today) && !isCompleted -> ErrorRed        // 과거 + 미작성
                    e.isBefore(today) -> SuccessGreen                    // 과거 + 완료
                    else -> WarningYellow                                // 미래
                }
                ColoredRange(s, e, color)
            } else null
        }.filter { r ->
            val a = YearMonth.from(r.start)
            val b = YearMonth.from(r.end)
            a == calYearMonth || b == calYearMonth
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 홈으로 돌아오면 최신 데이터로 동기화
                reviewVm.loadPosts()
                eduVm.loadPosts()
                communityVm.loadPosts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

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
        Log.d("HomeScreen", "Enter Free Detail branch: selectedPost.id=${selectedPost!!.id}")
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
                val detail: PostDetailResponse = freeDetailState.data
                Log.d("HomeScreen", "Load Free detail success")
                FreeBoardDetail(
                    post = selectedPost!!,
                    onBack = {
                        selectedPost = null
                        reviewVm.loadPosts()
                    },
                    vm = communityVm,
                    apiPost = detail
                )
            }
        }
        return
    }

    if(selectedEduProgram != null) {
        EduProgramDetail(
            program = selectedEduProgram!!,
            onBack = {
                selectedEduProgram = null
                eduVm.loadPosts()
            },
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
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(10.dp))
                Text("관측 캘린더", style = MaterialTheme.typography.titleLarge, color = TextHighlight)
                Spacer(Modifier.height(12.dp))

                CalendarCard(
                    yearMonth = calYearMonth,
                    selected = calSelected,
                    singleBadges = singleBadges,
                    ranges = ranges,
                    onSelect = { picked ->
                        calSelected = picked
                        showPlanSheet = true            //날짜 탭 시 시트 오픈
                    },
                    onPrev = { calYearMonth = calYearMonth.minusMonths(1) },
                    onNext = { calYearMonth = calYearMonth.plusMonths(1) },
                    containerColor = Purple100,
                    textColor = Purple900
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
//                if (locationState.lat != null && locationState.lon != null) {
//                    Text("위도(Lat): ${"%.5f".format(locationState.lat)}", fontSize = 16.sp, color = TextHighlight)
//                    Text("경도(Lon): ${"%.5f".format(locationState.lon)}", fontSize = 16.sp, color = TextHighlight)
//                }
//                if (locationState.address.isNotBlank()) {
//                    Spacer(Modifier.height(8.dp))
//                    Text("주소: ${locationState.address}", fontSize = 16.sp, color = TextHighlight)
//                }
//                Spacer(Modifier.height(24.dp))

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
                onFreePostClick = { postId ->
                    Log.d("HomeScreen", "onFreePostClick id=$postId")
                    val target = freeList.find { it.id == postId }
                    if (target != null) {
                        communityVm.resetPostDetail()
                        communityVm.selectPost(target.id)  // 기존 로직 유지 (VM에 id 전달)
                        selectedPost = target              // 상세 전환 트리거
                        Log.d("HomeScreen", "Selected free post -> id=${target.id}, title=${target.title}")
                    } else {
                        Log.w("HomeScreen", "Free post not found in freeList. id=$postId")
                    }
                },
                onSyncReviewLikeCount = { _, _ -> },
                enableInternalScroll = false,
                internalPadding = 16.dp
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
    if (showPlanSheet) {
        PlanBottomSheet(
            date = calSelected,
            plans = plansForSelectedDay,
            onDismiss = { showPlanSheet = false },
            onOpenScheduleScreen = {
                showPlanSheet = false
                onOpenSchedule()        //일정 화면으로 이동
            },
            onOpenDetail = { _ ->
                showPlanSheet = false
                onOpenSchedule()
            },
            onEdit = { _ ->
                showPlanSheet = false
                onOpenSchedule()
            },
            onDelete = { _ ->
                showPlanSheet = false
                onOpenSchedule()
            },
            onWriteReview = { _ -> showPlanSheet = false },
            alarmMinutesOf = { planId -> planVm.alarmMinutesOf(planId) },
            setAlarmMinutes = { _, _ -> /* no-op: 홈에서는 수정 불가 */ },
            onAlarm = { _, _ -> /* no-op: 홈에서는 버튼 눌러도 반응 없음 */ }
        )
    }
}