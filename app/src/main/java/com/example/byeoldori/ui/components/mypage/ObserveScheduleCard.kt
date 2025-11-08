package com.example.byeoldori.ui.components.mypage

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.data.model.dto.PlanDetailDto
import java.time.*
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.byeoldori.R
import com.example.byeoldori.data.model.dto.EventStatus
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.utils.SweObjUtils

data class ScheduleUiModel(
    val id: Long,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val placeName: String, //관측 대상
    val address: String,
    val memo: String?,
    val hasReview: Boolean
) { val isPast: Boolean get() = end.isBefore(LocalDateTime.now()) }

private val dateFmt = DateTimeFormatter.ofPattern("yy.MM.dd")
private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")


/** 서버 문자열을 유연하게 LocalDateTime으로 파싱 */
fun parseDateTimeFlexible(raw: String): LocalDateTime {
    // 1) ISO_ZONED/OFFSET 시도
    runCatching { return ZonedDateTime.parse(raw).toLocalDateTime() }
    runCatching { return OffsetDateTime.parse(raw).toLocalDateTime() }
    // 2) 초가 있는 ISO_LOCAL_DATE_TIME
    runCatching { return LocalDateTime.parse(raw) }
    // 3) 초 없는 패턴 (서버 예: 2025-11-05T20:50)
    val noSec = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    runCatching { return LocalDateTime.parse(raw, noSec) }
    // 4) 마지막 안전장치 (현재시각)
    return LocalDateTime.now()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObserveScheduleCard(
    item: PlanDetailDto,
    onEdit: (PlanDetailDto) -> Unit,
    onDelete: (PlanDetailDto) -> Unit,
    onWriteReview: (PlanDetailDto) -> Unit,
    onOpenDetail: (PlanDetailDto) -> Unit,
    modifier: Modifier = Modifier,
    onAlarm: (PlanDetailDto, Int) -> Unit = { _, _ -> },
    minutesBefore: Int,
    onMinutesChange: (Int) -> Unit,
) {
    val labelColor = Color.White.copy(alpha = 0.68f)
    val valueColor = Color.White
    val actionColor = Color.White.copy(alpha = 0.68f)

    val start = parseDateTimeFlexible(item.startAt)
    val end   = parseDateTimeFlexible(item.endAt)
    val isPast = end.isBefore(LocalDateTime.now())

    val isSameDay = start.toLocalDate() == end.toLocalDate()
    val datePart = start.format(dateFmt)
    val timePart = if (isSameDay) {
        "${start.format(timeFmt)} ~ ${end.format(timeFmt)}"
    } else {
        "${start.format(timeFmt)} ~ ${end.format(dateFmt)} ${end.format(timeFmt)}"
    }

    val displayPeriod = "$datePart $timePart"
    val targets = item.targets.firstOrNull()
        ?.takeIf { it.isNotBlank() }
        ?.let { SweObjUtils.toKorean(it) }
        ?: item.title
    val address = item.placeName ?: (item.observationSiteName ?: "—")

    Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onOpenDetail(item) }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(end = 72.dp)
                       // .clickable { onWriteReview(item) }
                ) {
                    Text(
                        text = item.title ?: targets ?: "제목 없음",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))

                    Text("관측 일자", style = MaterialTheme.typography.labelSmall, color = labelColor)
                    Spacer(Modifier.height(2.dp))
                    //val dateStr = item.start.format(dateFmt)
                    //val timeStr = "${item.start.format(timeFmt)}~${item.end.format(timeFmt)}"
                    Text(
                        text = displayPeriod, //"$dateStr $timeStr"
                        style = MaterialTheme.typography.bodyMedium,
                        color = valueColor
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "수정",
                        style = MaterialTheme.typography.labelMedium,
                        color = actionColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onEdit(item) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "삭제",
                        style = MaterialTheme.typography.labelMedium,
                        color = actionColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDelete(item) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LabeledTwoLine("관측 대상", targets, labelColor, valueColor)
                    Spacer(Modifier.height(6.dp))
                    LabeledTwoLine("관측지", address, labelColor, valueColor)
                    item.memo?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(6.dp))
                        LabeledTwoLine("메모", it, labelColor, valueColor, maxLines = 2)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    if(isPast) {
                        val isCompleted = item.status == EventStatus.COMPLETED
                        val buttonContainer = if (isCompleted) Color.White else Purple500
                        val buttonTextColor = if (isCompleted) Color.Black else Color.White
                        val buttonLabel = if (isCompleted) "리뷰 작성 완료" else "리뷰 작성"

                        Button(
                            onClick = {  if (!isCompleted) onWriteReview(item) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = buttonContainer.copy(alpha = 1f),
                                contentColor = buttonTextColor
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            enabled = !isCompleted
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "리뷰 작성",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(buttonLabel)
                        }
                    } else {
                        var menuExpanded by remember { mutableStateOf(false) }
                       // var minutesBefore by rememberSaveable(item.id) { mutableStateOf(60) } //기본 1시간 전
                        val context = LocalContext.current
                        fun labelFor(min: Int) = when (min) {
                            5 -> "5분 전 알림"
                            60 -> "1시간 전 알림"
                            120 -> "2시간 전 알림"
                            1440 -> "하루 전 알림"
                            else -> "${min}분 전 알림"
                        }
                        Box {
                            FilledTonalButton(
                                onClick = {
                                    onAlarm(item, minutesBefore) //즉시 예약
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Purple500,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_alarm),
                                    contentDescription = "알림 설정",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(labelFor(minutesBefore))

                                Spacer(Modifier.width(4.dp))
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDropDown,
                                        contentDescription = "다른 알림 선택",
                                        tint = Color.White
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(Purple500)
                            ) {
                                fun pick(min: Int) {
                                    onMinutesChange(min)   //ViewModel에 반영
                                    menuExpanded = false
                                }
                                DropdownMenuItem(text = { Text("5분 전 알림") },    onClick = { pick(5) })
                                DropdownMenuItem(text = { Text("1시간 전 알림") },  onClick = { pick(60) })
                                DropdownMenuItem(text = { Text("2시간 전 알림") },  onClick = { pick(120) })
                                DropdownMenuItem(text = { Text("하루 전 알림") },   onClick = { pick(1440) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledTwoLine(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    maxLines: Int = Int.MAX_VALUE
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = labelColor)
        Spacer(Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}

//@Preview(showBackground = true, backgroundColor = 0xFF1F1144)
//@Composable
//private fun ObserveScheduleCardPreview() {
//    ObserveScheduleCard(
//        item = ScheduleUiModel(
//            id = 1L,
//            start = LocalDateTime.now().minusDays(2).withHour(21).withMinute(0),
//            end = LocalDateTime.now().minusDays(2).withHour(23).withMinute(30),
//            placeName = "페르세우스 유성우",
//            address = "배타 공원",
//            memo = "은하수 촬영",
//            hasReview = false,
//
//        ),
//        onEdit = {},
//        onDelete = {},
//        onWriteReview = {}
//    )
//}