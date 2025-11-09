package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.byeoldori.data.model.dto.PlanDetailDto
import com.example.byeoldori.ui.theme.*
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanBottomSheet(
    date: LocalDate,
    plans: List<PlanDetailDto>,
    onDismiss: () -> Unit,
    onOpenScheduleScreen: () -> Unit, // 전체 보기 이동용
    onEdit: (PlanDetailDto) -> Unit,
    onDelete: (PlanDetailDto) -> Unit,
    onWriteReview: (PlanDetailDto) -> Unit,
    onOpenDetail: (PlanDetailDto) -> Unit,
    alarmMinutesOf: (planId: Long) -> Flow<Int>,
    setAlarmMinutes: (planId: Long, minutes: Int) -> Unit,
    onAlarm: (PlanDetailDto, Int) -> Unit
) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val maxScrollHeight = (configuration.screenHeightDp * 0.4f).dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Blue800,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "${date.year % 100}.${"%02d".format(date.monthValue)}.${"%02d".format(date.dayOfMonth)} 관측 일정",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))

            if(plans.isEmpty()) {
                Text(
                    text = "등록된 관측 일정이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = onOpenScheduleScreen,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Purple500,
                        contentColor = Color.White
                    )
                ) { Text("관측 일정 화면에서 보기") }
                Spacer(Modifier.height(12.dp))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxScrollHeight)
                ) {
                    item{
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(plans.size) { idx ->
                        val dto = plans[idx]
                        val minutes by alarmMinutesOf(dto.id).collectAsStateWithLifecycle(initialValue = 60)

                        ObserveScheduleCard(
                            item = dto,
                            onEdit = onEdit,
                            onDelete = onDelete,
                            onWriteReview = onWriteReview,
                            onOpenDetail = onOpenDetail,
                            modifier = Modifier.fillMaxWidth(),
                            minutesBefore = minutes,
                            onMinutesChange = { min -> setAlarmMinutes(dto.id, min) },
                            onAlarm = { plan, _ -> onAlarm(plan, minutes) }
                        )
                        if (idx != plans.lastIndex) {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    item{
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}