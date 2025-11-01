package com.example.byeoldori.eduprogram

import com.example.byeoldori.skymap.StellariumController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.InputStream
import javax.inject.Inject

/**
 * 🎓 EduEngine
 * - JSON 기반 학습 시나리오를 순차적으로 실행
 * - StellariumController를 통해 천구 제어 가능
 */
class EduEngine @Inject constructor() {

    private var scenarioJob: Job? = null
    private var controller: StellariumController? = null

    private val _log = MutableStateFlow("준비됨")
    val log = _log.asStateFlow()

    /**
     * ✅ 초기 세팅 (교육 모드 시작 시 1회 호출)
     * - Controller 등록
     * - Stellarium 초기 상태 세팅
     */
    fun initialize(stellariumController: StellariumController) {
        controller = stellariumController
        _log.value = "엔진 초기화 완료"

        // 🌌 천문 엔진 초기 설정
        controller?.apply {
            setEducationMode() // 몰입형 모드 진입
            toggleConstellations(true)
            toggleEquatorialGrid(false)
            toggleAzimuthalGrid(false)
            toggleAtmosphere(false)
            toggleLandscape(false)
            setFov(70.0)
            setViewDirection(180.0, 25.0)
        }
    }

    /**
     * 🎬 시나리오 실행
     * - JSON 파일 내 단계별 action 수행
     */
    suspend fun runScenario(inputStream: InputStream) {
        stop() // 이전 실행 중단
        scenarioJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                val json = JSONArray(inputStream.bufferedReader().use { it.readText() })
                for (i in 0 until json.length()) {
                    ensureActive()

                    val step = json.getJSONObject(i)
                    val action = step.optString("action")
                    val params = step.optJSONObject("params")

                    _log.value = "실행 중: $action"

                    // 🎯 액션 분기 처리
                    when (action) {
                        "show_constellation" -> {
                            val visible = params?.optBoolean("visible", true) ?: true
                            controller?.toggleConstellations(visible)
                        }
                        "move_camera" -> {
                            val yaw = params?.optDouble("yaw", 180.0) ?: 180.0
                            val pitch = params?.optDouble("pitch", 30.0) ?: 30.0
                            controller?.setViewDirection(yaw, pitch)
                        }
                        "set_fov" -> {
                            val fov = params?.optDouble("fov", 60.0) ?: 60.0
                            controller?.setFov(fov)
                        }
                        "toggle_atmosphere" -> {
                            val visible = params?.optBoolean("visible", false) ?: false
                            controller?.toggleAtmosphere(visible)
                        }
                        "delay" -> {
                            val ms = params?.optLong("ms", 1000L) ?: 1000L
                            delay(ms)
                        }
                        "speak" -> {
                            val text = params?.optString("text") ?: ""
                            _log.value = "💬 별도리: $text"
                            // TODO: TTS 연동 예정
                        }
                        else -> {
                            _log.value = "⚠️ 알 수 없는 명령: $action"
                        }
                    }

                    delay(1000)
                }

                _log.value = "✅ 시나리오 완료"
            } catch (e: Exception) {
                _log.value = "❌ 실행 오류: ${e.message}"
            }
        }
    }

    /**
     * ⏹️ 시나리오 중단
     */
    fun stop() {
        scenarioJob?.cancel()
        scenarioJob = null
        _log.value = "중단됨"
    }
}
