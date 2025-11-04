//package com.example.byeoldori.eduprogram
//
//import com.example.byeoldori.skymap.StellariumController
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.ensureActive
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import org.json.JSONArray
//import org.json.JSONObject
//import java.io.InputStream
//import javax.inject.Inject
//
///**
// * 🎓 EduEngine
// * - JSON 기반 학습 시나리오를 순차적으로 실행
// * - StellariumController를 통해 천구 제어
// */
//class EduEngine @Inject constructor() {
//
//    private var scenarioJob: Job? = null
//    private var controller: StellariumController? = null
//
//    private val _log = MutableStateFlow("준비됨")
//    val log = _log.asStateFlow()
//
//    /**
//     * ✅ 초기 세팅
//     * - initConfig를 기반으로 Stellarium 환경 설정
//     */
//    fun initialize(stellariumController: StellariumController, initConfig: JSONObject?) {
//        controller = stellariumController
//        _log.value = "엔진 초기화 중..."
//
//        controller?.apply {
//            // 🔹 모드 설정
//            if (initConfig?.optString("mode") == "education") {
//                setEducationMode()
//            }
//
//            // 🔹 시야각 설정
//            val fov = initConfig?.optDouble("fov", 70.0) ?: 70.0
//            setFov(fov)
//
//            // 🔹 카메라 방향 설정
//            initConfig?.optJSONObject("view")?.let {
//                val yaw = it.optDouble("yaw", 180.0)
//                val pitch = it.optDouble("pitch", 25.0)
//                setViewDirection(yaw, pitch)
//            }
//
//            // 🔹 표시 요소 토글
//            initConfig?.optJSONObject("toggles")?.let {
//                toggleConstellations(it.optBoolean("constellation", true))
//                toggleEquatorialGrid(it.optBoolean("equatorialGrid", false))
//                toggleAzimuthalGrid(it.optBoolean("azimuthalGrid", false))
//                toggleAtmosphere(it.optBoolean("atmosphere", false))
//                toggleLandscape(it.optBoolean("landscape", false))
//            }
//        }
//
//        _log.value = "✅ 엔진 초기화 완료"
//    }
//
//    /**
//     * 🎬 JSON 파일 전체 실행 (init + scenario)
//     */
//    suspend fun runFromJsonStream(inputStream: InputStream) {
//        stop()
//        scenarioJob = CoroutineScope(Dispatchers.Default).launch {
//            try {
//                val root = JSONObject(inputStream.bufferedReader().use { it.readText() })
//
//                // ✅ 초기 설정 (init)
//                val initConfig = root.optJSONObject("init")
//                initialize(controller ?: return@launch, initConfig)
//
//                // ✅ 시나리오 실행
//                val scenarioArray = root.optJSONArray("scenario")
//                if (scenarioArray != null) {
//                    runScenarioArray(scenarioArray)
//                } else {
//                    _log.value = "⚠️ 시나리오 없음"
//                }
//
//            } catch (e: Exception) {
//                _log.value = "❌ JSON 실행 오류: ${e.message}"
//            }
//        }
//    }
//
//    /**
//     * ▶️ 시나리오 배열 실행
//     */
//    suspend fun runScenarioArray(array: JSONArray) {
//        stop()
//        scenarioJob = CoroutineScope(Dispatchers.Default).launch {
//            try {
//                for (i in 0 until array.length()) {
//                    ensureActive()
//
//                    val step = array.getJSONObject(i)
//                    val action = step.optString("action")
//                    val params = step.optJSONObject("params")
//
//                    _log.value = "🚀 실행 중: $action"
//                    executeAction(action, params)
//
//                    // 각 스텝 사이 1초 텀
//                    delay(1000)
//                }
//
//                _log.value = "✅ 시나리오 완료"
//            } catch (e: Exception) {
//                _log.value = "❌ 시나리오 실행 오류: ${e.message}"
//            }
//        }
//    }
//
//    /**
//     * 🧩 개별 액션 실행 함수
//     */
//    private suspend fun executeAction(action: String, params: JSONObject?) {
//        when (action) {
//            "show_constellation" -> {
//                val visible = params?.optBoolean("visible", true) ?: true
//                controller?.toggleConstellations(visible)
//            }
//
//            "move_camera" -> {
//                val yaw = params?.optDouble("yaw", 180.0) ?: 180.0
//                val pitch = params?.optDouble("pitch", 25.0) ?: 25.0
//                controller?.setViewDirection(yaw, pitch)
//            }
//
//            "set_fov" -> {
//                val fov = params?.optDouble("fov", 70.0) ?: 70.0
//                controller?.setFov(fov)
//            }
//
//            "toggle_atmosphere" -> {
//                val visible = params?.optBoolean("visible", false) ?: false
//                controller?.toggleAtmosphere(visible)
//            }
//
//            "delay" -> {
//                val ms = params?.optLong("ms", 1000L) ?: 1000L
//                delay(ms)
//            }
//
//            "speak" -> {
//                val text = params?.optString("text") ?: ""
//                _log.value = "💬 별도리: $text"
//                // TODO: TTS, Live2D 연동
//            }
//
//            else -> _log.value = "⚠️ 알 수 없는 명령: $action"
//        }
//    }
//
//    /**
//     * ⏹️ 실행 중단
//     */
//    fun stop() {
//        scenarioJob?.cancel()
//        scenarioJob = null
//        _log.value = "🛑 중단됨"
//    }
//}
