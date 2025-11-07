package com.example.byeoldori.ui.components.mypage

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.AlarmManagerCompat
import com.example.byeoldori.data.model.dto.PlanDetailDto
import java.time.ZoneId
import java.time.ZonedDateTime

//알림 결과 표현
sealed class PlanAlarmResult {
    object Scheduled : PlanAlarmResult()                 // 예약 성공
    object PastTime : PlanAlarmResult()                  // 이미 지난 시각
    object ExactAlarmNotAllowed : PlanAlarmResult()      // 정확 알람 권한/허용 없음
    data class Error(val cause: Throwable) : PlanAlarmResult()
}

fun PlanAlarm(
    context: Context,
    plan: PlanDetailDto,
    minBefore: Int, //몇분 전인지
    title: String = plan.title ?: "관측 알림",
    autoRequestPermission: Boolean = true,  // ← 화면에서 처리하기 싫으면 true 유지
    toastOnResult: Boolean = true
) : PlanAlarmResult {

    val startRaw = plan.startAt
    val start = parseDateTimeFlexible(startRaw)
    val triggerAt = start.minusMinutes(minBefore.toLong()) //n분 전 시각
    val epochMs = triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val now = System.currentTimeMillis()

    //이미 지난 시각이면 등록하지 않음
    if (epochMs <= now) {
        if (toastOnResult) Toast.makeText(context, "이미 지난 시각이에요.", Toast.LENGTH_SHORT).show()
        return PlanAlarmResult.PastTime
    }
    val am = context.getSystemService(AlarmManager::class.java)

    //정확 알람 허용 확인
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
        if (autoRequestPermission) {
            //권한이 없으면 설정 화면 열기
            openExactAlarmSettings(context)
            if (toastOnResult) Toast.makeText(context, "정확 알람 사용을 허용해 주세요.", Toast.LENGTH_SHORT)
                .show()
        }
        return PlanAlarmResult.ExactAlarmNotAllowed
    }

    val intent = Intent(context, PlanAlarmReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("startAtRaw", startRaw)
        putExtra("minutesBefore", minBefore)
        putExtra("notifyId", (plan.id ?: 0L).toInt())
    }

    val reqCode = (((plan.id ?: 0L) * 1000L) + minBefore).toInt()
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
    val pi = PendingIntent.getBroadcast(context, reqCode, intent, flags)

    return try {
        // 보안 예외가 여기서 발생 가능 → 내부에서 처리
        AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, epochMs, pi)
        if (toastOnResult) {
            val msg = when (minBefore) {
                5 -> "5분 전 알림으로 저장했어요."
                60 -> "1시간 전 알림으로 저장했어요."
                120 -> "2시간 전 알림으로 저장했어요."
                1440 -> "하루 전 알림으로 저장했어요."
                else -> "${minBefore}분 전 알림으로 저장했어요."
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
        PlanAlarmResult.Scheduled
    } catch (se: SecurityException) {
        // 혹시 canScheduleExactAlarms()가 true라도 OEM/정책 이슈로 던질 수 있어 방어
        if (autoRequestPermission) {
            openExactAlarmSettings(context)
            if (toastOnResult) Toast.makeText(context, "정확 알람 사용을 허용해 주세요.", Toast.LENGTH_SHORT)
                .show()
        }
        PlanAlarmResult.ExactAlarmNotAllowed
    } catch (t: Throwable) {
        if (toastOnResult) Toast.makeText(context, "알림 예약에 실패했어요.", Toast.LENGTH_SHORT).show()
        PlanAlarmResult.Error(t)
    }
}

//권한이 없을 때, 설정 화면으로 이동
private fun openExactAlarmSettings(context: Context) {
    val i = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        setData(Uri.parse("package:${context.packageName}"))
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(i)
}


















