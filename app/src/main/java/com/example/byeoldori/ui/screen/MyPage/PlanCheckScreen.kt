package com.example.byeoldori.ui.screen.MyPage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.dto.PlanDetailDto
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.mypage.*
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.viewmodel.Community.PlanViewModel
import com.example.byeoldori.viewmodel.UiState
import java.time.*

enum class ObserveTab { Schedule, Review }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCheckScreen(
    schedules: List<ScheduleUiModel> = emptyList(),
    onBack: () -> Unit = {},
    onAddSchedule: () -> Unit = {},
    onEdit: (PlanDetailDto) -> Unit = {},
    onDelete: (PlanDetailDto) -> Unit = {},
    onWriteReview: (PlanDetailDto) -> Unit = {},
    planVm: PlanViewModel = hiltViewModel()
) {
    var currentTab by remember { mutableStateOf(ObserveTab.Schedule) }
    val bg = Brush.verticalGradient(listOf(Color(0xFF3B2377), Color(0xFF5B2F8F)))

    var isInReviewDetail by remember { mutableStateOf(false) } //Detail 진입 여부
    var showWriteForm by remember { mutableStateOf(false) }

    //오늘 날짜 기준으로 현재 월 데이터 불러오기
    val now = remember { LocalDate.now() }
    LaunchedEffect(now.year, now.monthValue) {
        planVm.loadMonthPlans(now.year, now.monthValue)
    }

    val planState by planVm.monthPlansState.collectAsState()
    val plans: List<PlanDetailDto> = remember(planState) {
        when (val s = planState) {
            is UiState.Success -> s.data
            else -> emptyList()
        }
    }

    val createState by planVm.createState.collectAsState()
    LaunchedEffect(createState) {
        if(createState is UiState.Success) {
            showWriteForm = false
            planVm.loadMonthPlans(now.year, now.monthValue)
        }
    }

    val monthState by planVm.monthPlansState.collectAsState()
    var selectedPlan by remember { mutableStateOf<PlanDetailDto?>(null) }
    var showDetail by remember { mutableStateOf(false) }

    if (showDetail && selectedPlan != null) {
        LaunchedEffect(selectedPlan!!.id) {
            planVm.loadPlanDetail(selectedPlan!!.id)
        }
        PlanDetail(
            plan = selectedPlan!!,
            onBack = { showDetail = false }
        )
    } else if (showWriteForm) {
        PlanWriteForm(
            onBack = { showWriteForm = false },
            initialPlan = selectedPlan
        )
    } else {
        Scaffold(
            //하단 네비게이션바 inset제외
            contentWindowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
            ),
            topBar = {
                if (!(currentTab == ObserveTab.Review && isInReviewDetail)) { //Review -> Detail모드일 때는 상단바 숨기기
                    Column {
                        Spacer(Modifier.height(40.dp))
                        TopBar(
                            title = "관측 일정 및 나의 관측 후기",
                            onBack = onBack,
                        )
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    //.background(bg)
                    .padding(top = innerPadding.calculateTopPadding()) //bottom에 패딩 주지 않기
            ) {
                when (currentTab) {
                    ObserveTab.Schedule -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(Modifier.height(8.dp))

                            TabRow(
                                selectedTabIndex = currentTab.ordinal,
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                                        color = Color.White
                                    )
                                },
                                divider = {}
                            ) {
                                Tab(
                                    selected = currentTab == ObserveTab.Schedule,
                                    onClick = { currentTab = ObserveTab.Schedule },
                                    text = {
                                        Text(
                                            text = "관측 일정",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    },
                                    selectedContentColor = Color.White,
                                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                                )
                                Tab(
                                    selected = currentTab == ObserveTab.Review,
                                    onClick = { currentTab = ObserveTab.Review },
                                    text = {
                                        Text(
                                            text = "나의 관측 후기",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    },
                                    selectedContentColor = Color.White,
                                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            Spacer(Modifier.height(12.dp))

                            FilledTonalButton(
                                onClick = {
                                    selectedPlan = null
                                    showWriteForm = true
                                }, //onAddSchedule
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Purple500, //0xFF9B7CFF
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .height(44.dp)
                            ) { Text("${"관측 계획 추가하기"}  +") }

                            Spacer(Modifier.height(8.dp))

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(items = plans, key = { it.id }) { dto ->
                                    ObserveScheduleCard(
                                        item = dto,
                                        onEdit = { plan ->           // ← 수정 버튼 누르면
                                            selectedPlan = plan
                                            showDetail = false       // 혹시 열려있을 수도 있으니 닫고
                                            showWriteForm = true     // ← WriteForm 열기
                                        },
                                        onDelete = onDelete,
                                        onWriteReview = {
                                            selectedPlan = dto          // ← 선택한 계획 기억
                                            showDetail = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    HorizontalDivider(
                                        color = Color.White.copy(alpha = 0.60f),
                                        thickness = 2.dp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    ObserveTab.Review -> {
                        Column(Modifier.fillMaxSize()) {
                            if (!isInReviewDetail) {
                                TabRow(
                                    selectedTabIndex = 1,
                                    containerColor = Color.Transparent,
                                    contentColor = Color.White,
                                    indicator = { tabPositions ->
                                        TabRowDefaults.SecondaryIndicator(
                                            modifier = Modifier.tabIndicatorOffset(tabPositions[1]),
                                            color = Color.White
                                        )
                                    },
                                    divider = {}
                                ) {
                                    Tab(
                                        selected = false,
                                        onClick = { currentTab = ObserveTab.Schedule },
                                        text = {
                                            Text(
                                                "관측 일정",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        },
                                        selectedContentColor = Color.White,
                                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                                    )
                                    Tab(
                                        selected = true,
                                        onClick = { },
                                        text = {
                                            Text(
                                                "나의 관측 후기",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        },
                                        selectedContentColor = Color.White,
                                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            MyReviewList(
                                onBack = { currentTab = ObserveTab.Schedule },
                                onDetailModeChange = { isInReviewDetail = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun demoSchedules(): List<ScheduleUiModel> {
    val now = LocalDateTime.now()
    return listOf(
        ScheduleUiModel(
            id = 1L,
            start = now.minusDays(10).withHour(22).withMinute(0),
            end = now.minusDays(10).withHour(22).withMinute(30),
            placeName = "보름달",
            address = "우리집 앞",
            memo = "사진 찍기",
            hasReview = true
        ),
        ScheduleUiModel(
            id = 2L,
            start = now.minusDays(5).withHour(21).withMinute(0),
            end = now.minusDays(5).withHour(23).withMinute(30),
            placeName = "페르세우스 유성우",
            address = "별마로 천문대",
            memo = "은하수 촬영",
            hasReview = false
        ),
        ScheduleUiModel(
            id = 3L,
            start = now.plusDays(1).withHour(22).withMinute(0),
            end = now.plusDays(2).withHour(1).withMinute(0),
            placeName = "목성",
            address = "배타 공원",
            memo = null,
            hasReview = false
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1F1144)
@Composable
private fun PlanCheckScreenPreview() {
    PlanCheckScreen(
        schedules = demoSchedules(),
        onBack = {},
        onAddSchedule = {},
        onEdit = {},
        onDelete = {},
        onWriteReview = {}
    )
}