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
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.ui.components.mypage.*
import com.example.byeoldori.ui.theme.*
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
    val observeCount = 0 //이건 추후 수정(내가 작성한 관측 후기 개수로 함)

    Background(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(20.dp))
        MyPageContent(
            baseMonth = baseMonth,
            selectedDate = selectedDate,
            onPrevMonth = { baseMonth = baseMonth.minusMonths(1) },
            onNextMonth = { baseMonth = baseMonth.plusMonths(1) },
            onSelectDate = { selectedDate = it },
            profileName = profileName,
            observationCount = observeCount,
            onOpenLikes = onOpenLikes,
            onOpenMyBoards = onOpenMyBoards,
            onOpenMyPrograms = onOpenMyPrograms
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
    onOpenMyPrograms: () -> Unit = {}
) {
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
                singleBadges = sampleBadges(baseMonth),
                ranges = sampleRanges(baseMonth),
                onSelect = onSelectDate,
                onPrev = onPrevMonth,
                onNext = onNextMonth
            )
        }
        item {
            MenuGroupCard(
                containerColor = Color(0xFF3D2A79),
                items = listOf(
                    MenuItem(title = "관측 일정 및 나의 관측 후기", onClick = {}), // onOpenSchedule
                    //MenuItem(title = "찜", onClick = {}), // onOpenBookmarks
                    MenuItem(title = "좋아요", onClick = onOpenLikes),
                    MenuItem(title = "내가 작성한 자유게시글", onClick = onOpenMyBoards),
                    MenuItem(title = "내가 작성한 교육 프로그램", onClick = onOpenMyPrograms),
                    MenuItem(title = "내가 작성한 댓글", onClick = {}), // onOpenMyComments
                    MenuItem(title = "고객 센터", onClick = {}), // onOpenSupport
                )
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
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
            onOpenLikes = {}
        )
    }
}