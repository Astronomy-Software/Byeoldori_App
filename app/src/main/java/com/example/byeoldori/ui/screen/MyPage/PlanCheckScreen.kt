package com.example.byeoldori.ui.screen.MyPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.mypage.ObserveScheduleCard
import com.example.byeoldori.ui.components.mypage.ScheduleUiModel
import java.time.LocalDateTime


enum class ObserveTab { Schedule, Review }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCheckScreen(
    schedules: List<ScheduleUiModel>,
    onBack: () -> Unit,
    onAddSchedule: () -> Unit,
    onEdit: (ScheduleUiModel) -> Unit,
    onDelete: (ScheduleUiModel) -> Unit,
    onWriteReview: (ScheduleUiModel) -> Unit,
) {
    var currentTab by remember { mutableStateOf(ObserveTab.Schedule) }
    val bg = Brush.verticalGradient(listOf(Color(0xFF3B2377), Color(0xFF5B2F8F)))

    Scaffold(
        topBar = {
            Column {
                TopBar(
                    title = "관측 일정 및 나의 관측 후기",
                    onBack = onBack,
                    
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
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
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
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
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                            )
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                FilledTonalButton(
                    onClick = onAddSchedule,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF9B7CFF),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) { Text("${"관측 계획 추가하기"}  +") }

                Spacer(Modifier.height(8.dp))

                if (currentTab == ObserveTab.Schedule) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(schedules, key = { _, it -> it.id }) { index, item ->
                            ObserveScheduleCard(
                                item = item,
                                onEdit = onEdit,
                                onDelete = onDelete,
                                onWriteReview = onWriteReview,
                                modifier = Modifier.fillMaxWidth()
                            )
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.60f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "나의 관측 후기가 여기에 표시됩니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
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