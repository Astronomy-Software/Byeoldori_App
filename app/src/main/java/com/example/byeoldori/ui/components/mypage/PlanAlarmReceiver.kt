package com.example.byeoldori.ui.components.mypage

import android.Manifest
import android.os.Build
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.byeoldori.R
import com.live2d.live2dview.MainActivity
import kotlinx.serialization.json.JsonNull.content
import java.time.format.DateTimeFormatter

class PlanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.ensureChannel(context)

        val title = intent.getStringExtra("title") ?: "관측 알림"
        val startAtRaw = intent.getStringExtra("startAtRaw") ?: ""
        val minutesBefore = intent.getIntExtra("minutesBefore", 60)
        val notifyId = intent.getIntExtra("notifyId", 0)

        val start = parseDateTimeFlexible(startAtRaw)
        val timeStr = start.format(DateTimeFormatter.ofPattern("MM.dd HH:mm"))
        val offsetLabel = when(minutesBefore) {
            5 -> "관측 5분 전이에요!"
            60 -> "관측 1시간 전이에요!"
            120 -> "관측 2시간 전이에요!"
            1440 -> "관측 하루 전이에요!"
            else -> "${minutesBefore}분 전"
        }

        val message = "$timeStr $offsetLabel"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPI = PendingIntent.getActivity(
            context,
            notifyId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)         // 상단 상태바에 뜨는 아이콘
            .setContentTitle(title)                    // 알림 제목: 관측 일정 제목
            .setContentText(message)                   // 알림 내용: "11.12 19:15 · 1시간 전이에요!"
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)                       // 클릭하면 사라지게
            .setContentIntent(openPI)                  // 클릭 시 앱으로 이동

        if (Build.VERSION.SDK_INT >= 33) {
            val ok = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!ok) return
        }

        NotificationManagerCompat.from(context).notify(notifyId, builder.build())
    }
}