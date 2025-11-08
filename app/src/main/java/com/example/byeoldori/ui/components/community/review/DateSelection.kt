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

//관측 날짜 + 시간
@Composable
fun DateTimeSelection(
    label: String = "관측 일시를 선택하세요",
    datetime: String,
    onPicked: (String) -> Unit,
    use24HourView: Boolean = true
){
    val context = LocalContext.current
    val cal = Calendar.getInstance()

    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    LaunchedEffect(datetime) {
        if (datetime.isNullOrBlank()) return@LaunchedEffect

        fun pad2(i: Int) = "%02d".format(i)

        // 케이스 A) "yyyy-MM-dd HH:mm HH:mm"
        val a = Regex("""^(\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2})\s+(\d{2}):(\d{2})$""")
        // 케이스 B) "yy.MM.dd HH:mm ~ HH:mm"
        val b = Regex("""^(\d{2})\.(\d{2})\.(\d{2})\s+(\d{2}):(\d{2})\s*~\s*(\d{2}):(\d{2})$""")

        when {
            a.matches(datetime) -> {
                val m = a.find(datetime)!!.groupValues
                val y = m[1].toInt(); val mo = m[2].toInt(); val d = m[3].toInt()
                val sh = m[4].toInt(); val sm = m[5].toInt()
                val eh = m[6].toInt(); val em = m[7].toInt()
                startDate = "%04d-%02d-%02d".format(y, mo, d)
                startTime = "${pad2(sh)}:${pad2(sm)}"
                endTime   = "${pad2(eh)}:${pad2(em)}"
            }
            b.matches(datetime) -> {
                val m = b.find(datetime)!!.groupValues
                val y2 = m[1].toInt(); val y = 2000 + y2 // 20xx 가정
                val mo = m[2].toInt(); val d = m[3].toInt()
                val sh = m[4].toInt(); val sm = m[5].toInt()
                val eh = m[6].toInt(); val em = m[7].toInt()
                startDate = "%04d-%02d-%02d".format(y, mo, d)
                startTime = "%02d:%02d".format(sh, sm)
                endTime   = "%02d:%02d".format(eh, em)
            }
            else -> {
                // 알 수 없는 포맷이면 그대로 둠
            }
        }
    }

    val displayDate = remember(startDate, startTime, endTime) {
        if (startDate.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()) {
            try {
                val p = startDate.split("-")
                val formatted = "%02d.%02d.%02d".format(
                    p[0].takeLast(2).toInt(),
                    p[1].toInt(),
                    p[2].toInt()
                )
                "$formatted $startTime ~ $endTime"
            } catch (e: Exception) {
                "$startDate $startTime ~ $endTime"
            }
        } else ""
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
                        startDate = "%04d-%02d-%02d".format(yy, mm + 1, dd)

                        TimePickerDialog(
                            context,
                            { _, sh, sm ->
                                startTime = "%02d:%02d".format(sh, sm)

                                TimePickerDialog(
                                    context,
                                    { _, eh, em ->
                                        endTime = "%02d:%02d".format(eh, em)
                                        onPicked("$startDate $startTime $endTime")
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    use24HourView
                                ).apply { setTitle("관측 종료 시각") }.show()
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            use24HourView
                        ).apply {setTitle("관측 시작 시각") }.show()
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                    ).apply { setTitle("관측 일자") }.show()
                }
                .padding(vertical = 8.dp)
        ) {
            if (displayDate.isEmpty()) {
                Text(
                    "관측 일자를 선택해주세요",
                    fontSize = 14.sp,
                    color = TextHighlight.copy(alpha = 0.5f)
                )
            } else {
                Text(displayDate, fontSize = 14.sp, color = TextHighlight)
            }
        }
    }

    fun emitPicked() {
        if (startDate.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()) {
            // PlanWriteForm.parseDateTimes가 기대하는 포맷 유지
            onPicked("$startDate $startTime $endTime")
        }
    }
}