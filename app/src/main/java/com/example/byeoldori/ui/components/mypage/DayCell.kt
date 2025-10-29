package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.byeoldori.ui.theme.ErrorRed
import java.time.LocalDate

@Composable
fun DayCell(
    date: LocalDate?,
    inThisMonth: Boolean,
    isSelected: Boolean,
    textColor: Color,
    singleColor: Color?,
    rangeRole: RangeRole?,
    rangeColor: Color?,
    onClick: () -> Unit,
    isToday: Boolean,
    size: Dp
) {
    val baseAlpha = if (inThisMonth) 1f else 0.35f
    val fgDefault = textColor.copy(alpha = baseAlpha)
    val hasRange = rangeRole != null && rangeColor != null

    Box(
        modifier = Modifier
            .size(size)
            .noRippleClickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date == null) { } else {
            val dayText = date.dayOfMonth.toString()

            when {
                isToday -> CircularBadge(
                    text = dayText,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary
                )

                isSelected -> CircularBadge(
                    text = dayText,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                hasRange && rangeColor != null -> CircularBadge(
                    text = dayText,
                    backgroundColor = rangeColor,
                    textColor = Color.White
                )

                singleColor != null -> CircularBadge(
                    text = dayText,
                    backgroundColor = singleColor,
                    textColor = Color.White
                )

                // 기본 텍스트
                else -> Text(
                    text = dayText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = fgDefault
                )
            }
        }
    }
}

@Composable
private fun CircularBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}

fun Modifier.noRippleClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val src = remember { MutableInteractionSource() }
    clickable(interactionSource = src, indication = null, enabled = enabled, onClick = onClick)
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2, widthDp = 64)
@Composable
private fun PreviewDayCell_PillMiddle() {
    DayCell(
        date = LocalDate.of(2025, 10, 10),
        inThisMonth = true,
        isSelected = false,
        textColor = Color(0xFF351E5B),
        singleColor = null,
        rangeRole = RangeRole.MIDDLE,
        rangeColor = ErrorRed,
        onClick = {},
        isToday = false,
        size = 44.dp
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2, widthDp = 64)
@Composable
private fun PreviewDayCell_Today() {
    DayCell(
        date = LocalDate.now(),
        inThisMonth = true,
        isSelected = false,
        textColor = Color(0xFF351E5B),
        singleColor = null,
        rangeRole = null,
        rangeColor = null,
        onClick = {},
        isToday = true,
        size = 44.dp
    )
}