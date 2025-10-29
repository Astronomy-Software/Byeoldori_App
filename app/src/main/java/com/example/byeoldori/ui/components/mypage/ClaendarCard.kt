package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.ErrorRed
import com.example.byeoldori.ui.theme.Purple100
import com.example.byeoldori.ui.theme.Purple900
import com.example.byeoldori.ui.theme.SuccessGreen
import com.example.byeoldori.ui.theme.WarningYellow
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ColoredRange(val start: LocalDate, val end: LocalDate, val color: Color)
enum class RangeRole { START, MIDDLE, END, None }

@Composable
fun CalendarCard(
    yearMonth: YearMonth,
    selected: LocalDate,
    singleBadges: Map<LocalDate, Color>,
    ranges: List<ColoredRange>,
    onSelect: (LocalDate) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    containerColor: Color = Purple100,
    textColor: Color = Purple900,
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    val days = remember(yearMonth) { buildMonthDays(yearMonth) }
    val today = remember { androidx.compose.runtime.mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val waitMs = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0)
            delay(waitMs)
            today.value = LocalDate.now()
        }
    }

    Surface(color = containerColor, shape = shape, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                val headerText = today.value.format(
                    DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
                )
                Text(
                    headerText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* 자리만 */ }) {
                    Icon(Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = textColor.copy(alpha = 0.9f))
                }
            }
            Spacer(Modifier.height(4.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(6.dp))

            // 월 전후 이동
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrev, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.ArrowBackIosNew,
                        contentDescription = stringResource(R.string.prev_month),
                        tint = textColor)
                }
                Text(
                    "${yearMonth.year}년 ${yearMonth.monthValue}월",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = textColor)
                )
                IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.ArrowForwardIos,
                        contentDescription = stringResource(R.string.next_month),
                        tint = textColor)
                }
            }

            Spacer(Modifier.height(2.dp))

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val columns = 7
                val hSpacing = 0.dp
                val vSpacing = 8.dp
                val cell = (maxWidth - hSpacing * (columns - 1)) / columns

                val rows = (days.size / 7).coerceAtLeast(1)
                val gridHeight = cell * rows + vSpacing * (rows - 1)

                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(hSpacing)
                    ) {
                        listOf("일","월","화","수","목","금","토").forEach {
                            Text(
                                it,
                                color = textColor.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(cell),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        userScrollEnabled = false,
                        verticalArrangement = Arrangement.spacedBy(vSpacing),
                        horizontalArrangement = Arrangement.spacedBy(hSpacing),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight)
                    ) {
                        items(days.size) { idx ->
                            val date = days[idx]
                            val (role, rangeColor) = date?.let { rangeRoleFor(it, ranges) } ?: (null to null)
                            val singleColor = if (role == null && date != null) singleBadges[date] else null

                            DayCell(
                                date = date,
                                inThisMonth = date?.let { YearMonth.from(it) == yearMonth } ?: false,
                                isSelected = (date == selected),
                                textColor = textColor,
                                singleColor = singleColor,
                                rangeRole = role,
                                rangeColor = rangeColor,
                                onClick = { if (date != null) onSelect(date) },
                                isToday = (date == LocalDate.now()),
                                size = cell
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun buildMonthDays(ym: YearMonth): List<LocalDate?> {
    val first = ym.atDay(1)
    val startBlank = first.dayOfWeek.value % 7 // Sun=0
    val days = ym.lengthOfMonth()
    val list = MutableList(startBlank) { null as LocalDate? }
    repeat(days) { i -> list.add(ym.atDay(i + 1)) }
    while (list.size % 7 != 0) list.add(null)
    return list
}

private fun rangeRoleFor(date: LocalDate, ranges: List<ColoredRange>): Pair<RangeRole?, Color?> {
    ranges.forEach { r ->
        if (!date.isBefore(r.start) && !date.isAfter(r.end)) {
            val role = when (date) {
                r.start -> RangeRole.START
                r.end -> RangeRole.END
                else -> RangeRole.MIDDLE
            }
            return role to r.color
        }
    }
    return null to null
}

@Preview(showBackground = true, backgroundColor = 0xFF2B184F, widthDp = 360)
@Composable
private fun PreviewCalendarCard() {
    val ym = YearMonth.of(2025, 10)
    CalendarCard(
        yearMonth = ym,
        selected = ym.atDay(22),
        singleBadges = mapOf(
            ym.atDay(1) to SuccessGreen,
            ym.atDay(25) to WarningYellow,
        ),
        ranges = listOf(ColoredRange(ym.atDay(10), ym.atDay(11), ErrorRed)),
        onSelect = {},
        onPrev = {},
        onNext = {},
        containerColor = Purple100,
        textColor = Purple900
    )
}