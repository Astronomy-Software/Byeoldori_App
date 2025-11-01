package com.example.byeoldori.skymap.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.ui.theme.Purple800
import com.example.byeoldori.ui.theme.TextHighlight

@Composable
fun ObjectInfoCard(
    name: String,
    type: String,
    otherNames: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    val joinedNames = if (otherNames.isNotEmpty()) otherNames.joinToString(", ") else "없음"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Purple800)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 이름
            Text(
                text = name,
                fontSize = 32.sp,
                color = TextHighlight,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))
            Text("종류: $type", color = TextHighlight)
            Spacer(Modifier.height(4.dp))

            Text(
                text = "다른 이름 : $joinedNames",
                color = TextHighlight,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            // "더보기/접기" 토글 버튼
            if (joinedNames.length > 30 || otherNames.size > 2) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (expanded) "접기 ▴" else "더보기 ▾",
                    color = Color.LightGray,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(top = 2.dp)
                )
            }
        }
    }
}