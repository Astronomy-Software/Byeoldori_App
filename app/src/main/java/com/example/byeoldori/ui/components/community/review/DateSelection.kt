package com.example.byeoldori.ui.components.community.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.example.byeoldori.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import android.app.DatePickerDialog
import android.app.TimePickerDialog

@Composable
fun DateSelection(
    label: String = "관측 일시를 선택하세요",
    date: String,
    onPicked: (date: String) -> Unit,
    use24HourView: Boolean = true
) {
    val context = LocalContext.current
    val cal = Calendar.getInstance()
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH)
    val d = cal.get(Calendar.DAY_OF_MONTH)
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val min = cal.get(Calendar.MINUTE)

    // UI 출력할 때 변환
    val displayDate = try {
        val parts = date.split("-")
        "%02d.%02d.%02d".format(parts[0].takeLast(2).toInt(), parts[1].toInt(), parts[2].toInt())
    } catch (e: Exception) {
        date // 실패하면 원본 그대로
    }
    Column {
        Text(
            label,
            color = TextDisabled,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // 날짜 선택
                    DatePickerDialog(context, { _, yy, mm, dd ->
                        val pickedDate = "%04d-%02d-%02d".format(yy, mm + 1, dd)
                        onPicked(pickedDate) //
                    }, y, m, d).apply { setTitle("관측 일자") }.show()
                }
                .padding(vertical = 8.dp)
        ) {
            if (date.isEmpty()) {
                Text(
                    "관측 일자를 선택해주세요",
                    fontSize = 14.sp,
                    color = TextHighlight.copy(alpha = 0.5f)
                )
            } else {
                Text(
                    displayDate,
                    fontSize = 14.sp,
                    color = TextHighlight
                )
            }
        }
    }
}