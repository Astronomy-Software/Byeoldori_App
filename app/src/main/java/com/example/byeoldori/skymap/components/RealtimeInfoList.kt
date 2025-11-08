package com.example.byeoldori.skymap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.byeoldori.skymap.ObjectItem


@Composable
fun RealtimeInfoList(items: List<ObjectItem>) {
    if (items.isEmpty()) {
        Text("실시간 데이터 없음", color = Color.LightGray)
        return
    }

    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            RealtimeItemCard(
                item = item,
                modifier = Modifier
                    .fillMaxWidth(0.49f) // ✅ 화면 너비의 절반으로 강제
                    .heightIn(min = 100.dp)
            )
        }
    }
}