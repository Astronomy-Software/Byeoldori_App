package com.example.byeoldori.ui.components.mypage

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_ID = "plan_alerts"  // 알림 채널 ID
    private const val CHANNEL_NAME = "관측 일정 알림"
    private const val CHANNEL_DESC = "관측 시작 전 알림을 제공합니다."

    /** 앱 실행 시 혹은 알림 띄우기 전에 호출 */
    fun ensureChannel(context: Context) {
        // Android 8.0 이상에서만 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }

            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}