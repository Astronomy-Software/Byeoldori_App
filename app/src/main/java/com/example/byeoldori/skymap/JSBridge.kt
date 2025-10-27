package com.example.byeoldori.skymap

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject

class AppBridge(
    private val context: Context,
    private val gyroController: GyroCameraController
) {

    @JavascriptInterface
    fun postMessage(data: String) {
        val json = JSONObject(data)
        val type = json.optString("type")

        when (type) {

            // 👁️ Eye Tracking 토글 처리
            "eye_tracking_toggle" -> {
                val enabled = json.getJSONObject("payload").optBoolean("enabled", false)
                if (enabled) {
                    gyroController.start()
                } else {
                    gyroController.stop()
                }
                Log.d("AppBridge", "Eye Tracking 토글: $enabled")
            }

            // 🌟 객체 선택 시
            "object_selected" -> {
                val payload = json.getJSONObject("payload")
                val name = payload.optString("name")
                val ra = payload.optJSONArray("radec")?.optDouble(0)
                val dec = payload.optJSONArray("radec")?.optDouble(1)
                Log.d("AppBridge", "선택됨: $name (RA=$ra, Dec=$dec)")
            }

            // ❌ 객체 선택 해제 시
            "object_unselected" -> {
                Log.d("AppBridge", "선택 해제됨")
            }

            // 🪛 기타 타입
            else -> {
                Log.w("AppBridge", "알 수 없는 메시지 타입: $type")
            }
        }
    }
}
