//package com.example.byeoldori.eduprogram
//
//import com.example.byeoldori.character.Emotion
//import com.example.byeoldori.character.Live2DControllerViewModel
//import com.example.byeoldori.skymap.StellariumController
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.cancelAndJoin
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.json.JSONArray
//import org.json.JSONObject
//import javax.inject.Inject
//
//const val CYGNUS_SCENARIO_JSON = """
//{
//  "init": {
//    "programTitle": "별도리 천체관측 프로그램: 백조자리 탐험",
//    "mode": "education",
//    "fov": 60,
//    "view": { "yaw": 60, "pitch": 25 },
//    "toggles": {
//      "constellation": false,
//      "equatorialGrid": false,
//      "azimuthalGrid": false,
//      "atmosphere": true,
//      "landscape": true
//    }
//  },
//  "scenario": [
//    {
//      "title": "백조자리 들어가기",
//      "steps": [
//        {
//          "live2d": { "text": "안녕! 오늘은 백조자리를 볼 거야.", "emotion": "Happy" },
//          "sky": { "camera": { "yaw": 60, "pitch": 20 } },
//          "duration": 9600
//        },
//        {
//          "live2d": { "text": "은하수 한가운데를 가로지르는 별자리야." },
//          "duration": 8000
//        }
//      ]
//    },
//    {
//      "title": "꼬리별 데네브",
//      "steps": [
//        {
//          "live2d": { "text": "백조자리의 꼬리 끝 데네브를 볼까?", "emotion": "Idle" },
//          "sky": { "object": "NAME Deneb" },
//          "duration": 9600
//        },
//        {
//          "live2d": { "text": "데네브는 아랍어로 '꼬리'라는 뜻이야.", "emotion": "Happy" },
//          "duration": 10000
//        }
//      ]
//    },
//    {
//      "title": "여름철 대삼각형",
//      "steps": [
//        {
//          "live2d": { "text": "이제 데네브를 기준으로 대삼각형을 연결해볼까?", "emotion": "Her" },
//          "sky": { "object": "NAME Deneb" },
//          "duration": 8000
//        },
//        {
//          "sky": { "object": "NAME Vega" },
//          "duration": 6000
//        },
//        {
//          "sky": { "object": "NAME Altair" },
//          "duration": 6000
//        }
//      ]
//    }
//  ]
//}
//"""
//
//class EduEngine @Inject constructor() {
//    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
//
//    private var scenarioArray: JSONArray? = null
//    private var currentSectionIndex = -1
//    private var currentStepIndex = -1
//
//    private var runningJob: Job? = null
//    private var timerJob: Job? = null
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading = _isLoading.asStateFlow()
//
//    private val _isReady = MutableStateFlow(false)
//    val isReady = _isReady.asStateFlow()
//
//    private val _isStarted = MutableStateFlow(false)
//    val isStarted = _isStarted.asStateFlow()
//
//    private val _isEnded = MutableStateFlow(false)
//    val isEnded = _isEnded.asStateFlow()
//
//    private val _autoPlay = MutableStateFlow(false)
//    val autoPlay = _autoPlay.asStateFlow()
//
//    private val _log = MutableStateFlow("엔진 대기 중")
//    val log = _log.asStateFlow()
//
//    private val _programTitle = MutableStateFlow("")
//    val programTitle = _programTitle.asStateFlow()
//
//    private val _totalSections = MutableStateFlow(0)
//    val totalSections = _totalSections.asStateFlow()
//
//    private val _currentSection = MutableStateFlow(-1)
//    val currentSection = _currentSection.asStateFlow()
//
//    private val _currentTitle = MutableStateFlow("")
//    val currentTitle = _currentTitle.asStateFlow()
//
//    private val _timerRemaining = MutableStateFlow(0L)
//    val timerRemaining = _timerRemaining.asStateFlow()
//
//    fun toggleAutoPlay() {
//        _autoPlay.value = !_autoPlay.value
//        _log.value = if (_autoPlay.value) "▶ 자동 넘김 켜짐" else "⏸ 자동 넘김 꺼짐"
//    }
//
//    fun stop() {
//        scope.launch {
//            runningJob?.cancelAndJoin()
//            timerJob?.cancelAndJoin()
//            _timerRemaining.value = 0L
//            _isStarted.value = false
//            _log.value = "🛑 시나리오 중단됨"
//        }
//    }
//
//    fun loadScenarioWithLoading(jsonProvider: () -> String) {
//        scope.launch {
//            resetAll()
//            _isLoading.value = true
//
//            Live2DControllerViewModel.playAppearanceMotion()
//            Live2DControllerViewModel.chat("교육 프로그램 준비 중이에요!", Emotion.Idle)
//
//            val json = withContext(Dispatchers.IO) { jsonProvider() }
//            delay(10_000)
//
//            val root = JSONObject(json)
//            initialize(root.optJSONObject("init"))
//
//            scenarioArray = root.optJSONArray("scenario") ?: JSONArray()
//            _totalSections.value = scenarioArray!!.length()
//
//            currentSectionIndex = 0
//            currentStepIndex = -1
//            _currentSection.value = 0
//
//            _isLoading.value = false
//            _isReady.value = true
//
//            Live2DControllerViewModel.chat("준비 다 됐어! 교육을 시작해볼까?", Emotion.Happy)
//            _log.value = "✅ 준비 완료 - Start 버튼을 눌러 시작하세요"
//        }
//    }
//
//    fun start() {
//        if (_isReady.value && !_isStarted.value) {
//            _isStarted.value = true
//            Live2DControllerViewModel.chat("그럼 시작해볼까?", Emotion.Happy)
//            nextStep()
//        }
//    }
//
//    private fun resetAll() {
//        scenarioArray = null
//        currentSectionIndex = -1
//        currentStepIndex = -1
//        _isLoading.value = false
//        _isReady.value = false
//        _isStarted.value = false
//        _isEnded.value = false
//        _programTitle.value = ""
//        _totalSections.value = 0
//        _currentSection.value = -1
//        _currentTitle.value = ""
//        _timerRemaining.value = 0L
//        _log.value = "엔진 대기 중"
//    }
//
//    private fun initialize(init: JSONObject?) {
//        val sc = StellariumController
//
//        if (init?.optString("mode") == "education")
//            sc.setEducationMode()
//
//        sc.setFov(init?.optDouble("fov", 60.0) ?: 60.0)
//
//        init?.optJSONObject("view")?.let {
//            sc.setViewDirection(
//                it.optDouble("yaw", 180.0),
//                it.optDouble("pitch", 25.0)
//            )
//        }
//
//        init?.optJSONObject("toggles")?.let {
//            sc.toggleConstellations(it.optBoolean("constellation", true))
//            sc.toggleEquatorialGrid(it.optBoolean("equatorialGrid", false))
//            sc.toggleAzimuthalGrid(it.optBoolean("azimuthalGrid", false))
//            sc.toggleAtmosphere(it.optBoolean("atmosphere", true))
//            sc.toggleLandscape(it.optBoolean("landscape", true))
//        }
//
//        _programTitle.value =
//            init?.optString("programTitle") ?: "천체관측 교육 프로그램"
//    }
//
//    fun nextStep() =
//        runStep(currentSectionIndex, currentStepIndex + 1)
//
//    fun prevStep() =
//        runStep(currentSectionIndex, (currentStepIndex - 1).coerceAtLeast(0))
//
//    private fun runStep(sectionIndex: Int, stepIndex: Int) {
//        val arr = scenarioArray ?: return
//        if (!_isStarted.value || sectionIndex !in 0 until arr.length()) return
//
//        val section = arr.getJSONObject(sectionIndex)
//        val steps = section.optJSONArray("steps") ?: JSONArray()
//
//        if (stepIndex !in 0 until steps.length()) {
//            moveToNextSectionOrEnd(sectionIndex, arr)
//            return
//        }
//
//        currentSectionIndex = sectionIndex
//        currentStepIndex = stepIndex
//
//        _currentSection.value = sectionIndex
//        _currentTitle.value = section.optString("title", "STEP ${sectionIndex + 1}")
//
//        scope.launch {
//            runningJob?.cancelAndJoin()
//            timerJob?.cancelAndJoin()
//            _timerRemaining.value = 0L
//
//            runningJob = launch {
//                val stepObj = steps.getJSONObject(stepIndex)
//                runSingleStep(stepObj)
//
//                val duration = stepObj.optLong("duration", 0L)
//
//                if (_autoPlay.value && duration > 0)
//                    startTimer(duration) { nextStep() }
//                else if (duration > 0)
//                    startTimer(duration) {}
//            }
//        }
//    }
//
//    private fun moveToNextSectionOrEnd(sectionIndex: Int, arr: JSONArray) {
//        val next = sectionIndex + 1
//
//        if (next >= arr.length()) {
//            _isEnded.value = true
//            _isStarted.value = false
//            _log.value = "🎉 모든 교육이 완료되었습니다."
//            return
//        }
//
//        currentStepIndex = -1
//        runStep(next, 0)
//    }
//
//    private fun startTimer(duration: Long, onFinished: () -> Unit) {
//        timerJob?.cancel()
//        timerJob = scope.launch {
//            val end = System.currentTimeMillis() + duration
//
//            while (isActive && System.currentTimeMillis() < end) {
//                _timerRemaining.value = end - System.currentTimeMillis()
//                delay(80)
//            }
//
//            if (isActive) onFinished()
//        }
//    }
//
//    private suspend fun runSingleStep(step: JSONObject) {
//        step.optJSONObject("live2d")?.let {
//            val msg = it.optString("text", "")
//            val emo = Emotion.values()
//                .find { e -> e.name == it.optString("emotion", "Idle") }
//                ?: Emotion.Idle
//
//            Live2DControllerViewModel.chat(msg, emo)
//        }
//
//        step.optJSONObject("sky")?.let { sky ->
//            val sc = StellariumController
//
//            sky.optJSONObject("camera")?.let {
//                sc.setViewDirection(
//                    it.optDouble("yaw", 180.0),
//                    it.optDouble("pitch", 25.0)
//                )
//            }
//
//            sky.optString("object")
//                ?.takeIf { it.isNotBlank() }
//                ?.let { sc.setLookUpObject("\"$it\"") }
//        }
//    }
//}
