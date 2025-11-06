package com.example.byeoldori.ui.screen.MyPage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.byeoldori.data.TestUserScreen
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.data.model.dto.PlanDetailDto
import com.example.byeoldori.ui.components.mypage.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.PlanViewModel
import com.example.byeoldori.viewmodel.UiState
import java.time.*

@Composable
fun MyPageScreen(
    onOpenSchedule: () -> Unit = {},
    onOpenBookmarks: () -> Unit = {},
    onOpenLikes: () -> Unit = {},
    onOpenMyBoards: () -> Unit = {},
    onOpenMyPrograms: () -> Unit = {},
    onOpenMyComments: () -> Unit = {},
    onOpenSupport: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    planVm: PlanViewModel = hiltViewModel()
) {

    var baseMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                baseMonth = YearMonth.now()
                selectedDate = LocalDate.now()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val userVm: UserViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        userVm.getMyProfile()
    }

    val me = userVm.userProfile.collectAsState().value
    val profileName = me?.nickname?.takeIf { it.isNotBlank() } ?: "익명"
    val observeCount = 0  //TODO: 이것도 연결해야 함

    //현재 월이 바뀔 때마다 월간 일정 로딩
    LaunchedEffect(baseMonth) {
        planVm.loadMonthPlans(baseMonth.year, baseMonth.monthValue)
    }

    val monthUi by planVm.monthPlansState.collectAsStateWithLifecycle()
    val plans = when(val s = monthUi) {
        is UiState.Success -> s.data
        else -> emptyList()
    }

    //단일 관측일 때
    val singleBadges = remember(baseMonth, plans) {
        val today = LocalDate.now()
        plans
            .mapNotNull { runCatching { parseDateTimeFlexible(it.startAt).toLocalDate() }.getOrNull() }
            .filter { YearMonth.from(it) == baseMonth }
            .distinct()
            .associateWith { date ->
                if (date.isBefore(today)) SuccessGreen    // 과거 일정 → 초록
                else WarningYellow                        // 오늘 이후 일정 → 노랑
            }
    }

    //자정을 넘길 때
    val ranges = remember(baseMonth, plans) {
        val today = LocalDate.now()
        plans.mapNotNull { p->
            val s = runCatching { parseDateTimeFlexible(p.startAt).toLocalDate() }.getOrNull()
            val e = runCatching { parseDateTimeFlexible(p.endAt).toLocalDate() }.getOrNull()
            if(s != null && e != null && s != e) {
                val color = if(e.isBefore(today)) SuccessGreen else WarningYellow
                ColoredRange(s, e, color)
            } else null
        }.filter { r ->
            val a = YearMonth.from(r.start)
            val b = YearMonth.from(r.end)
            a == baseMonth || baseMonth == b
        }
    }

    var showPlanSheet by remember { mutableStateOf(false) }

    val plansForSelectedDay = remember(selectedDate, plans) {
        plans.filter { p->
            val s = runCatching { parseDateTimeFlexible(p.startAt).toLocalDate() }.getOrNull()
            val e = runCatching { parseDateTimeFlexible(p.endAt).toLocalDate() }.getOrNull()
            if (s == null || e == null) return@filter false
            !selectedDate.isBefore(s) && !selectedDate.isAfter(e)
        }
    }

    Background(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(20.dp))
        MyPageContent(
            baseMonth = baseMonth,
            selectedDate = selectedDate,
            onPrevMonth = { baseMonth = baseMonth.minusMonths(1) },
            onNextMonth = { baseMonth = baseMonth.plusMonths(1) },
            onSelectDate = { date ->
                selectedDate = date
                showPlanSheet = true
            },
            profileName = profileName,
            observationCount = observeCount,
            onOpenLikes = onOpenLikes,
            onOpenMyBoards = onOpenMyBoards,
            onOpenMyPrograms = onOpenMyPrograms,
            onOpenMyComments = onOpenMyComments,
            onOpenSchedule = onOpenSchedule,
            onOpenSettings = onOpenSettings,
            singleBadges = singleBadges,
            ranges = ranges
        )
    }
    if (showPlanSheet) {
        PlanBottomSheet(
            date = selectedDate,
            plans = plansForSelectedDay,
            onDismiss = { showPlanSheet = false },
            onOpenScheduleScreen = {
                showPlanSheet = false
                onOpenSchedule()
            },
            onEdit = {
                showPlanSheet = false
                onOpenSchedule()
            },
            onDelete = {
                showPlanSheet = false
                onOpenSchedule()
            },
            onWriteReview = {
                showPlanSheet = false
                onOpenSchedule()
            }
        )
    }
}

@Composable
private fun MyPageContent(
    baseMonth: YearMonth,
    selectedDate: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onEditProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    profileName: String,
    observationCount: Int,
    onOpenLikes: () -> Unit = {},
    onOpenMyBoards: () -> Unit = {},
    onOpenMyPrograms: () -> Unit = {},
    onOpenMyComments: () -> Unit = {},
    onOpenSchedule: () -> Unit = {},
    userVm: UserViewModel = hiltViewModel(),
    singleBadges: Map<LocalDate, Color>,
    ranges: List<ColoredRange>
) {
    var showDaySheet by remember { mutableStateOf(false) }
    var sheetDate by remember { mutableStateOf<LocalDate?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item { Spacer(Modifier.height(20.dp)) }
        item{
            ProfileCard(
                name = profileName,
                observationCount = observationCount,
                onEditProfile = onEditProfile,
                onOpenSettings = onOpenSettings
            )
            Spacer(Modifier.height(12.dp))
        }

        item {
            Text(
                text = "관측 캘린더",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )
        }
        item {
            CalendarCard(
                yearMonth = baseMonth,
                selected = selectedDate,
                singleBadges = singleBadges,
                ranges = ranges,
                onSelect =  { date ->
                    onSelectDate(date)
                },
                onPrev = onPrevMonth,
                onNext = onNextMonth
            )
        }
        item {
            MenuGroupCard(
                containerColor = Color(0xFF3D2A79),
                items = listOf(
                    MenuItem(title = "관측 일정 및 나의 관측 후기", onClick = onOpenSchedule),
                    MenuItem(title = "좋아요", onClick = onOpenLikes),
                    MenuItem(title = "내가 작성한 자유게시글", onClick = onOpenMyBoards),
                    MenuItem(title = "내가 작성한 교육 프로그램", onClick = onOpenMyPrograms),
                    MenuItem(title = "내가 작성한 댓글", onClick = onOpenMyComments),
                    MenuItem(title = "고객 센터", onClick = {}), // onOpenSupport
                    MenuItem(title = "설정", onClick = onOpenSettings),
                )
            )
        }
        item { Spacer(Modifier.height(12.dp)) }

        item {
            Divider(
                color = Color.White.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            TestUserScreen(vm = userVm)
        }
    }
}

private fun sampleBadges(ym: YearMonth) = mapOf(
    ym.atDay(1) to SuccessGreen,
    ym.atDay(25) to WarningYellow
)
private fun sampleRanges(ym: YearMonth) = listOf(
    ColoredRange(ym.atDay(10), ym.atDay(11), ErrorRed),
)

@Preview(showBackground = true, backgroundColor = 0xFF2B184F, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewMyPageContent() {
    Background {
        MyPageContent(
            baseMonth = YearMonth.of(2025, 10),
            selectedDate = LocalDate.of(2025, 10, 22),
            onPrevMonth = {},
            onNextMonth = {},
            onSelectDate = {},
            profileName = "별도리",
            observationCount = 123,
            onOpenLikes = {},
            onOpenMyBoards = {},
            onOpenMyPrograms = {},
            onOpenMyComments = {},
            onOpenSchedule = {},
            onOpenSettings = {},
            singleBadges = mapOf(YearMonth.of(2025, 10).atDay(1) to SuccessGreen),
            ranges = listOf(ColoredRange(YearMonth.of(2025, 10).atDay(10), YearMonth.of(2025, 10).atDay(11), ErrorRed))
        )
    }
}