package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.ui.theme.*

@Composable
fun PlanDescription(
    textColor: Color,
    doneColor: Color = SuccessGreen,
    upcomingColor: Color = WarningYellow,
    missingReviewColor: Color = ErrorRed
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ColorItem(dotColor = doneColor, label = "관측 완료", textColor = textColor)
        ColorItem(dotColor = upcomingColor, label = "관측 예정", textColor = textColor)
        ColorItem(dotColor = missingReviewColor, label = "관측 완료·미작성", textColor = textColor)
    }
}

@Composable
private fun ColorItem(
    dotColor: Color,
    label: String,
    textColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(color = textColor.copy(alpha = 0.85f))
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2B184F, widthDp = 400)
@Composable
private fun PreviewPlanStatusLegend() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        PlanDescription(textColor = Purple900)
    }
}
