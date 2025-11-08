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
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.ui.components.mypage.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.PlanViewModel
import com.example.byeoldori.viewmodel.UiState
import java.time.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

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
    planVm: PlanViewModel = hiltViewModel(),
) {

    var baseMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

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
            android.widget.Toast.makeText(ctx, "알림 권한이 필요합니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

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

    var showProfile by remember { mutableStateOf(false) }
    var showProfileEdit by remember { mutableStateOf(false) }
    val userVm: UserViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        userVm.getMyProfile()
        planVm.loadObservationCount()
    }

    val me = userVm.userProfile.collectAsState().value
    val profileName = me?.nickname?.takeIf { it.isNotBlank() } ?: "익명"
    val observationCount by planVm.observationCount.collectAsStateWithLifecycle()

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
        buildMap<LocalDate, Color> {
            plans.forEach { p ->
                val s = runCatching { parseDateTimeFlexible(p.startAt).toLocalDate() }.getOrNull()
                val e = runCatching { parseDateTimeFlexible(p.endAt).toLocalDate() }.getOrNull()

                // 하루짜리 일정만 해당 달에 표시
                if (s != null && e != null && s == e && YearMonth.from(s) == baseMonth) {
                    val isCompleted = (p.status?.name == "COMPLETED")
                    val color = when {
                        s.isBefore(today) && !isCompleted -> ErrorRed    // 과거 + 리뷰 미작성
                        s.isBefore(today) -> SuccessGreen                 // 과거 + 완료
                        else -> WarningYellow                             // 미래
                    }

                    // 같은 날짜에 여러 일정이 있을 경우: 빨강 > 초록 > 노랑
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
    val ranges = remember(baseMonth, plans) {
        val today = LocalDate.now()
        plans.mapNotNull { p ->
            val s = runCatching { parseDateTimeFlexible(p.startAt).toLocalDate() }.getOrNull()
            val e = runCatching { parseDateTimeFlexible(p.endAt).toLocalDate() }.getOrNull()

            if (s != null && e != null && s != e) {
                val isCompleted = (p.status?.name == "COMPLETED")
                val color = when {
                    e.isBefore(today) && !isCompleted -> ErrorRed   // 과거 + 리뷰 미작성
                    e.isBefore(today) -> SuccessGreen               // 과거 + 완료
                    else -> WarningYellow                           // 미래
                }
                ColoredRange(s, e, color)
            } else null
        }.filter { r ->
            val a = YearMonth.from(r.start)
            val b = YearMonth.from(r.end)
            a == baseMonth || b == baseMonth
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

    if (showProfileEdit) {
        ProfileEditCard(
            onSaved = {
                // 저장 후 프로필 상세로 이동하거나 마이페이지로 복귀 등 원하는 흐름 선택
                showProfileEdit = false
                showProfile = true
            },
            onBack = { showProfileEdit = false }
        )
        return
    }

    if (showProfile) {
        // 프로필 상세 화면
        MyProfileContent(
            onBack = { showProfile = false }
        )
        return
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
            onOpenProfile = { showProfile = true },
            onEditProfile = { showProfileEdit = true },
            profileName = profileName,
            observationCount = observationCount,
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
            },
            onOpenDetail = {
                showPlanSheet = false
                onOpenSchedule()
            },
            getAlarmMinutes = { planId -> planVm.getAlarmMinutes(planId) },
            setAlarmMinutes = { planId, minutes -> planVm.setAlarmMinutes(planId, minutes) },

            onAlarm = { plan, minutes ->
                if (Build.VERSION.SDK_INT >= 33 && !notifGranted) {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@PlanBottomSheet
                }
                PlanAlarm(
                    context = ctx,
                    plan = plan,
                    minBefore = minutes,
                    autoRequestPermission = true,   // 정확 알람 허용 없으면 설정으로 유도
                    toastOnResult = true            // 결과 토스트는 내부에서 처리
                )
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
    onOpenProfile: () -> Unit,
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item { Spacer(Modifier.height(20.dp)) }
            item {
                ProfileCard(
                    name = profileName,
                    observationCount = observationCount,
                    onEditProfile = onEditProfile,
                    onOpenProfile = onOpenProfile
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
                    onSelect = { date ->
                        onSelectDate(date)
                    },
                    onPrev = onPrevMonth,
                    onNext = onNextMonth
                )
            }
            item {
                var showResignConfirm by remember { mutableStateOf(false) }
                MenuGroupCard(
                    containerColor = Color(0xFF3D2A79),
                    items = listOf(
                        MenuItem(title = "관측 일정 및 나의 관측 후기", onClick = onOpenSchedule),
                        MenuItem(title = "좋아요", onClick = onOpenLikes),
                        MenuItem(title = "내가 작성한 자유게시글", onClick = onOpenMyBoards),
                        MenuItem(title = "내가 작성한 교육 프로그램", onClick = onOpenMyPrograms),
                        MenuItem(title = "내가 작성한 댓글", onClick = onOpenMyComments),
                        MenuItem(title = "고객 센터", onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "아직 준비중인 기능입니다",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }), // onOpenSupport
                        MenuItem(title = "설정", onClick = onOpenSettings),
                        MenuItem(title = "로그아웃", onClick = { userVm.logOut() }, color = ErrorRed),
                        MenuItem(
                            title = "회원 탈퇴",
                            onClick = { showResignConfirm = true },
                            color = ErrorRed
                        )
                    )
                )
                if (showResignConfirm) {
                    AlertDialog(
                        onDismissRequest = { showResignConfirm = false },
                        title = { Text("회원 탈퇴", color = Color.Black) },
                        text = { Text("정말로 탈퇴하시겠어요? 이 작업은 되돌릴 수 없습니다.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showResignConfirm = false
                                userVm.resign()
                            }) {
                                Text("탈퇴")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResignConfirm = false }) {
                                Text("취소")
                            }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Purple500,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
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

//@Preview(showBackground = true, backgroundColor = 0xFF2B184F, widthDp = 360, heightDp = 800)
//@Composable
//private fun PreviewMyPageContent() {
//    Background {
//        MyPageContent(
//            baseMonth = YearMonth.of(2025, 10),
//            selectedDate = LocalDate.of(2025, 10, 22),
//            onPrevMonth = {},
//            onNextMonth = {},
//            onSelectDate = {},
//            profileName = "별도리",
//            observationCount = 123,
//            onOpenLikes = {},
//            onOpenMyBoards = {},
//            onOpenMyPrograms = {},
//            onOpenMyComments = {},
//            onOpenSchedule = {},
//            onOpenSettings = {},
//            singleBadges = mapOf(YearMonth.of(2025, 10).atDay(1) to SuccessGreen),
//            ranges = listOf(ColoredRange(YearMonth.of(2025, 10).atDay(10), YearMonth.of(2025, 10).atDay(11), ErrorRed)),
//
//        )
//    }
//}