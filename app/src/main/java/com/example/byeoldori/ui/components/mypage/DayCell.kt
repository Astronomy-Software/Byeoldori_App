package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
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

    val pillShape = when (rangeRole) {
        RangeRole.START -> RoundedCornerShape(topStart = 999.dp, bottomStart = 999.dp)
        RangeRole.MIDDLE -> RoundedCornerShape(0.dp)
        RangeRole.END -> RoundedCornerShape(topEnd = 999.dp, bottomEnd = 999.dp)
        else -> RoundedCornerShape(0.dp)
    }

    val bg = when {
        rangeRole != null -> rangeColor ?: Color.Transparent
        singleColor != null -> singleColor
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(size)
            //.then(badgeBorderModifier)
            .noRippleClickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            val dayText = date.dayOfMonth.toString()

            if(hasRange) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(pillShape)
                        .background(rangeColor!!)
                )
            }
            val badgeBorderModifier = if (isSelected) {
                Modifier.border(2.dp, Color.Black, CircleShape)
            } else {
                Modifier
            }

            when {
                isToday -> CircularBadge(
                    text = dayText,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = badgeBorderModifier
                )

                hasRange && rangeColor != null -> CircularBadge(
                    text = dayText,
                    backgroundColor = rangeColor,
                    textColor = Color.White,
                    modifier = badgeBorderModifier
                )

                singleColor != null -> CircularBadge(
                    text = dayText,
                    backgroundColor = singleColor,
                    textColor = Color.White,
                    modifier = badgeBorderModifier
                )

                isSelected -> Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = fgDefault
                    )
                }

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
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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