
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import com.example.byeoldori.skymap.ObjectDetailViewModel
import com.example.byeoldori.skymap.ObjectItem
import com.example.byeoldori.skymap.SkyCameraController
import com.example.byeoldori.skymap.SkyObjectDetail
import org.json.JSONObject

class AppBridge(
    private val context: Context,
    private val gyroController: SkyCameraController,
    private val viewModel: ObjectDetailViewModel
) {
    @JavascriptInterface
    fun postMessage(data: String) {
        try {
            val json = JSONObject(data)
            val type = json.optString("type")

            when (type) {

                // ✅ 초기화 완료 이벤트
                "stel_ready" -> {
                    Log.i("AppBridge", "✅ Stellarium Web Engine 초기화 완료 신호 수신")
                    viewModel.onSweEngineReady()
                }

                // 👁️ Eye Tracking
                "eye_tracking_toggle" -> {
                    val enabled = json.getJSONObject("payload").optBoolean("enabled", false)
                    if (enabled) gyroController.start() else gyroController.stop()
                    Log.d("AppBridge", "🎯 Eye Tracking 토글: $enabled")
                }

                // 🌟 상세 정보 보기
                "show_object_detail" -> {
                    val payload = json.optJSONObject("payload") ?: return
                    val show = payload.optBoolean("show", true) // ✅ show 필드 사용

                    if (show) {
                        val name = payload.optString("name")
                        val typeStr = payload.optString("type")
                        val wiki = payload.optString("wikipediaData", "정보 없음")

                        val otherNames = mutableListOf<String>()
                        payload.optJSONArray("otherNames")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                otherNames.add(arr.optString(i))
                            }
                        }

                        val detail = SkyObjectDetail(
                            name = name,
                            type = typeStr,
                            wikipediaSummary = wiki,
                            otherNames = otherNames
                        )

                        viewModel.updateSelectedObject(detail)
                        viewModel.setDetailVisible(true) // ✅ 패널 표시
                        Log.i("AppBridge", "📦 상세 정보 표시 요청: $name")
                    } else {
                        viewModel.clearSelection() // ✅ show=false 시 숨김 처리
                        Log.i("AppBridge", "❌ 상세 정보 숨김 요청")
                    }
                }

                // 🛰 실시간 업데이트
                "object_items" -> {
                    val payload = json.optJSONObject("payload") ?: return
                    val itemsArray = payload.optJSONArray("items")
                    val items = mutableListOf<ObjectItem>()
                    if (itemsArray != null) {
                        for (i in 0 until itemsArray.length()) {
                            val item = itemsArray.getJSONObject(i)
                            items.add(
                                ObjectItem(
                                    key = item.optString("key"),
                                    value = item.optString("value")
                                )
                            )
                        }
                    }
                    viewModel.updateRealtimeItems(items)
                }

                // ❌ 선택 해제
                "object_unselected" -> {
                    viewModel.clearSelection()
                    Log.d("AppBridge", "🪐 객체 선택 해제됨")
                }

                else -> Log.w("AppBridge", "⚠️ 알 수 없는 타입: $type")
            }

        } catch (e: Exception) {
            Log.e("AppBridge", "❌ JSON 파싱 오류: ${e.message}\n원본 데이터: $data")
        }
    }
}
