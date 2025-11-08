package com.example.byeoldori.ui.screen.MyPage

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.byeoldori.data.model.dto.PlanDetailDto
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.mypage.*
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.viewmodel.Community.PlanViewModel
import com.example.byeoldori.viewmodel.UiState
import java.time.*
import java.time.format.DateTimeFormatter
import android.Manifest
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.dto.EventStatus
import com.example.byeoldori.data.model.dto.UpdatePlanRequest
import com.example.byeoldori.ui.components.community.review.ReviewWriteForm
import com.example.byeoldori.utils.SweObjUtils
import com.example.byeoldori.viewmodel.Community.ReviewViewModel

fun parseDateTimeFlexible(raw: String): LocalDateTime {
    runCatching { return ZonedDateTime.parse(raw).toLocalDateTime() }
    runCatching { return OffsetDateTime.parse(raw).toLocalDateTime() }
    runCatching { return LocalDateTime.parse(raw) }
    val noSec = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    runCatching { return LocalDateTime.parse(raw, noSec) }
    return LocalDateTime.now()
}

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
    planVm: PlanViewModel
) {
    var currentTab by remember { mutableStateOf(ObserveTab.Schedule) }
    val bg = Brush.verticalGradient(listOf(Color(0xFF3B2377), Color(0xFF5B2F8F)))

    var isInReviewDetail by remember { mutableStateOf(false) } //Detail 진입 여부
    var showWriteForm by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    var ym by remember { mutableStateOf(YearMonth.of(today.year, today.monthValue)) }

    LaunchedEffect(ym) { // 현재 ym이 바뀔때마다 해당 월 로딩
        planVm.loadMonthPlans(ym.year, ym.monthValue)
    }

    val ui by planVm.monthPlansState.collectAsStateWithLifecycle()
    val createState by planVm.createState.collectAsState()
    val updateState by planVm.updateState.collectAsState()
    val deleteState by planVm.deleteState.collectAsState()

    LaunchedEffect(createState, showWriteForm) {
        if (!showWriteForm && createState is UiState.Success) {
            planVm.loadMonthPlans(ym.year, ym.monthValue)
            planVm.resetCreateState()
        }
    }

    LaunchedEffect(updateState, showWriteForm) {
        if (!showWriteForm && updateState is UiState.Success) {
            planVm.loadMonthPlans(ym.year, ym.monthValue)
            planVm.resetUpdateState()
        } else if (!showWriteForm && updateState is UiState.Error) {
            planVm.resetUpdateState()
        }
    }

    LaunchedEffect(deleteState) {
        when (deleteState) {
            is UiState.Success, is UiState.Error -> planVm.resetDeleteState()
            else -> Unit
        }
    }

    var selectedPlan by remember { mutableStateOf<PlanDetailDto?>(null) }
    var showDetail by remember { mutableStateOf(false) }
    var confirmDeleteTarget by remember { mutableStateOf<PlanDetailDto?>(null) }
    var showReviewForm by remember { mutableStateOf(false) }

    if (showReviewForm && selectedPlan != null) {
        val plan = selectedPlan!!
        val start = parseDateTimeFlexible(plan.startAt)
        val prefillTargets = plan.targets
            .map { SweObjUtils.toKorean(it) }        // ← 여기서 한국어로 변환
            .joinToString(", ")
        val reviewVm: ReviewViewModel = hiltViewModel()

        ReviewWriteForm(
            author = "me",
            vm = reviewVm,
            onCancel = { showReviewForm = false },
            onSubmit = {
                selectedPlan?.let { plan ->
                    planVm.updatePlan(
                        id = plan.id,
                        body = UpdatePlanRequest(status = EventStatus.COMPLETED)
                    )
                }
                planVm.loadMonthPlans(ym.year, ym.monthValue)
                showReviewForm = false
                currentTab = ObserveTab.Review
            },
            onTempSave = {},
            onMore = {},
            prefillTitle = plan.title ?: plan.targets.firstOrNull().orEmpty(),
            prefillTargets = prefillTargets,
            prefillSite = plan.placeName ?: (plan.observationSiteName ?: ""),
            prefillDate = "%04d-%02d-%02d".format(start.year, start.monthValue, start.dayOfMonth),
        )
        return  // 폼이 열렸을 땐 다른 화면들 렌더링 안 함
    }

    if (showDetail && selectedPlan != null) {
        LaunchedEffect(selectedPlan!!.id) {
            planVm.loadPlanDetail(selectedPlan!!.id)
        }
        PlanDetail(
            plan = selectedPlan!!,
            onBack = { showDetail = false },
            planVm = planVm
        )
        return
    }
    if (showWriteForm) {
        PlanWriteForm(
            onBack = {
                showWriteForm = false
                planVm.loadMonthPlans(ym.year, ym.monthValue)
            },
            planVm = planVm,
            initialPlan = selectedPlan
        )
        return
    }

    val ctx = LocalContext.current
    var notifGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33)
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifGranted = granted
        if (!granted) {
            // 원하면 스낵바/토스트 안내 추가 가능
            android.widget.Toast.makeText(ctx, "알림 권한이 필요합니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

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
                        MonthNavigator(
                            ym = ym,
                            onPrev = { ym = ym.minusMonths(1) },
                            onNext = { ym = ym.plusMonths(1) },
                            textColor = Color.White
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.60f), thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))
                        Spacer(Modifier.height(8.dp))

                        val allPlans: List<PlanDetailDto> = when (val s = ui) {
                            is UiState.Success -> s.data
                            else -> emptyList()
                        }
                        LaunchedEffect(allPlans) {
                            Log.d("PlanScreen", "plans visible=${allPlans.map { it.id }}")
                        }

                        val monthPlans = remember(ym, allPlans) { //현재 YearMonth에 속하는 일정만 필터링
                            allPlans.filter {
                                try { YearMonth.from(parseDateTimeFlexible(it.startAt).toLocalDate()) == ym }
                                catch (e: Exception) { false }
                            }
                        }

                        when (ui) {
                            is UiState.Loading -> {
                                Box(Modifier.fillMaxWidth().padding(16.dp)) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                            is UiState.Error -> {
                                val message = (ui as UiState.Error).message
                                Text(
                                    text = message ?: "목록을 불러오지 못했습니다.",
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            is UiState.Idle, is UiState.Success -> {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(items = monthPlans, key = { it.id }) { dto ->
                                        val ctx = LocalContext.current
                                        ObserveScheduleCard(
                                            item = dto,
                                            minutesBefore = planVm.getAlarmMinutes(dto.id),
                                            onMinutesChange = { min -> planVm.setAlarmMinutes(dto.id, min) },
                                            onEdit = { plan ->           //수정 버튼 누르면
                                                planVm.resetCreateState()
                                                planVm.resetUpdateState()
                                                selectedPlan = plan
                                                showDetail = false       // 혹시 열려있을 수도 있으니 닫고
                                                showReviewForm = false
                                                showWriteForm = true     // WriteForm 열기
                                            },
                                            onDelete = { plan ->
                                                confirmDeleteTarget = plan
                                            },
                                            onWriteReview = {
                                                selectedPlan = dto          //선택한 계획 기억
                                                showDetail = false
                                                showWriteForm = false
                                                showReviewForm = true
                                            },
                                            onAlarm = { plan, _ ->
                                                if (Build.VERSION.SDK_INT >= 33 && !notifGranted) {
                                                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                    return@ObserveScheduleCard
                                                }
                                                val min = planVm.getAlarmMinutes(plan.id)
                                                PlanAlarm(ctx, plan, min, autoRequestPermission = true, toastOnResult = true)
                                            },
                                            onOpenDetail = { plan ->
                                                selectedPlan = plan
                                                showWriteForm = false
                                                showReviewForm = false
                                                showDetail = true
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.60f), thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                }
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
            if (confirmDeleteTarget != null) {
                AlertDialog(
                    onDismissRequest = { confirmDeleteTarget = null },
                    title = { Text("관측 계획 삭제",color = Color.Black) },
                    text  = { Text("관측 계획을 삭제할까요?") },
                    confirmButton = { TextButton(onClick = {
                        val id = confirmDeleteTarget?.id ?: return@TextButton
                        confirmDeleteTarget = null
                        planVm.deletePlan(id, ym.year, ym.monthValue)
                    }) { Text("삭제") } },
                    dismissButton = { TextButton(onClick = { confirmDeleteTarget = null }) { Text("취소") } }
                )
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

//@Preview(showBackground = true, backgroundColor = 0xFF1F1144)
//@Composable
//private fun PlanCheckScreenPreview() {
//    PlanCheckScreen(
//        schedules = demoSchedules(),
//        onBack = {},
//        onAddSchedule = {},
//        onEdit = {},
//        onDelete = {},
//        onWriteReview = {},
//
//    )
//}