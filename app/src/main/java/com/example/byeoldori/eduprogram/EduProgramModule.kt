package com.example.byeoldori.eduprogram

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.character.Emotion
import com.example.byeoldori.character.Live2DControllerViewModel
import com.example.byeoldori.skymap.SkyMode
import com.example.byeoldori.skymap.StellariumController
import com.example.byeoldori.skymap.StellariumScreen
import com.example.byeoldori.ui.theme.TextHighlight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

// ===============================================================
// ✅ JSON 로더
// ===============================================================
fun loadJsonFromAssets(context: Context, path: String): String {
    return context.assets.open(path).bufferedReader().use { it.readText() }
}

// ===============================================================
// ✅ 상태 FSM
// ===============================================================
sealed class EduState {
    object Loading : EduState()
    object Ready : EduState()
    object Started : EduState()
    object Ended : EduState()
}

// ===============================================================
// ✅ EduEngine (모든 교육 로직)
// ===============================================================
class EduEngine @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var scenarioArray: JSONArray? = null
    private var currentSectionIndex = -1
    private var currentStepIndex = -1

    private var runningJob: Job? = null
    private var timerJob: Job? = null

    private val _state = MutableStateFlow<EduState>(EduState.Loading)
    val state = _state.asStateFlow()

    private val _autoPlay = MutableStateFlow(true)
    val autoPlay = _autoPlay.asStateFlow()

    private val _log = MutableStateFlow("엔진 대기 중")
    val log = _log.asStateFlow()

    private val _programTitle = MutableStateFlow("")
    val programTitle = _programTitle.asStateFlow()

    private val _totalSections = MutableStateFlow(0)
    val totalSections = _totalSections.asStateFlow()

    private val _currentSection = MutableStateFlow(-1)
    val currentSection = _currentSection.asStateFlow()

    private val _currentTitle = MutableStateFlow("")
    val currentTitle = _currentTitle.asStateFlow()

    private val _timerRemaining = MutableStateFlow(0L)
    val timerRemaining = _timerRemaining.asStateFlow()

    private val _currentStepDuration = MutableStateFlow(0L)
    val currentStepDuration = _currentStepDuration.asStateFlow()


    fun toggleAutoPlay() { _autoPlay.value = !_autoPlay.value }

    fun loadScenarioWithLoading(jsonProvider: () -> String) {
        scope.launch {
            resetAll()
            _state.value = EduState.Loading

            Live2DControllerViewModel.playAppearanceMotion()
            Live2DControllerViewModel.chat("교육 프로그램 준비 중이에요!", Emotion.Idle)

            val json = withContext(Dispatchers.IO) { jsonProvider() }

            delay(6000)

            val root = JSONObject(json)
            initialize(root.optJSONObject("init"))
            scenarioArray = root.optJSONArray("scenario") ?: JSONArray()

            _totalSections.value = scenarioArray!!.length()
            currentSectionIndex = 0
            currentStepIndex = -1
            _currentSection.value = 0

            _state.value = EduState.Ready
            Live2DControllerViewModel.chat("준비 다 됐어! 교육을 시작해볼까?", Emotion.Happy)
        }
    }

    fun start() {
        if (_state.value is EduState.Ready) {
            _state.value = EduState.Started
            Live2DControllerViewModel.chat("그럼 시작해볼까?", Emotion.Happy)
            nextStep()
        }
    }

    private fun resetAll() {
        scenarioArray = null
        currentSectionIndex = -1
        currentStepIndex = -1

        _programTitle.value = ""
        _totalSections.value = 0
        _currentSection.value = -1
        _currentTitle.value = ""
        _timerRemaining.value = 0L
        _currentStepDuration.value = 0L
        _log.value = "엔진 대기 중"

        runningJob?.cancel()
        timerJob?.cancel()
    }

    private fun initialize(init: JSONObject?) {
        val sc = StellariumController
        if (init?.optString("mode") == "education") sc.setEducationRightBarMode()

        sc.setFov(init?.optDouble("fov", 60.0) ?: 60.0)

        init?.optJSONObject("view")?.let {
            sc.setViewDirection(
                it.optDouble("yaw", 0.0),
                it.optDouble("pitch", 45.0)
            )
        }

        if (init != null) {
            init.optJSONObject("toggles")?.let {
                sc.toggleConstellations(it.optBoolean("constellation", false))
                sc.toggleEquatorialGrid(it.optBoolean("equatorialGrid", false))
                sc.toggleAzimuthalGrid(it.optBoolean("azimuthalGrid", false))
                sc.toggleAtmosphere(it.optBoolean("atmosphere", false))
                sc.toggleLandscape(it.optBoolean("landscape", false))
            }
        }

        _programTitle.value = init?.optString("programTitle") ?: "천체관측 교육 프로그램"
    }

    fun nextStep() = runStep(currentSectionIndex, currentStepIndex + 1)
    fun prevStep() = runStep(currentSectionIndex, maxOf(currentStepIndex - 1, 0))

    private fun runStep(sectionIndex: Int, stepIndex: Int) {
        val arr = scenarioArray ?: return
        if (_state.value !is EduState.Started) return
        if (sectionIndex < 0 || sectionIndex >= arr.length()) return

        val section = arr.getJSONObject(sectionIndex)
        val steps = section.optJSONArray("steps") ?: JSONArray()

        if (stepIndex >= steps.length()) {
            moveToNextSectionOrEnd(sectionIndex, arr)
            return
        }

        currentSectionIndex = sectionIndex
        currentStepIndex = stepIndex
        _currentSection.value = sectionIndex
        _currentTitle.value = section.optString("title", "")

        scope.launch {
            runningJob?.cancel()
            timerJob?.cancel()

            runningJob = launch {
                val obj = steps.getJSONObject(stepIndex)
                runSingleStep(obj)

                val duration = obj.optLong("duration", 0L)
                _currentStepDuration.value = duration

                if (_autoPlay.value && duration > 0) {
                    startTimer(duration) { nextStep() }
                } else {
                    _timerRemaining.value = 0L
                }
            }
        }
    }

    private fun moveToNextSectionOrEnd(current: Int, arr: JSONArray) {
        val next = current + 1
        if (next >= arr.length()) {
            _state.value = EduState.Ended
            _currentTitle.value = ""
            _log.value = "🎉 모든 교육이 완료되었습니다."
            return
        }

        currentStepIndex = -1
        _currentTitle.value = ""
        runStep(next, 0)
    }

    private fun startTimer(duration: Long, onFinished: () -> Unit) {
        timerJob?.cancel()
        timerJob = scope.launch {
            val end = System.currentTimeMillis() + duration

            while (isActive && System.currentTimeMillis() < end) {
                _timerRemaining.value = end - System.currentTimeMillis()
                delay(70)
            }

            if (isActive) onFinished()
        }
    }
    private suspend fun runSingleStep(step: JSONObject) {

        // ✅ 1) Live2D 처리
        step.optJSONObject("live2d")?.let { live ->
            val text = live.optString("text", "")
            val emotion = Emotion.entries
                .find { e -> e.name == live.optString("emotion", "Idle") }
                ?: Emotion.Idle

            Live2DControllerViewModel.chat(text, emotion)
        }

        // ✅ 2) Sky 처리
        step.optJSONObject("sky")?.let { sky ->
            val sc = StellariumController

            // ----- (A) 카메라 이동 -----
            sky.optJSONObject("camera")?.let { cam ->
                val yaw = cam.optDouble("yaw", Double.NaN)
                val pitch = cam.optDouble("pitch", Double.NaN)

                if (!yaw.isNaN() && !pitch.isNaN()) {
                    sc.setViewDirection(yaw, pitch)
                }
            }

            // ----- (B) FOV 변경 -----
            if (sky.has("fov")) {
                sc.setFov(sky.optDouble("fov", 60.0))
            }

            // ----- (C) 객체 이름으로 선택 -----
            if (sky.has("object")) {
                val name = sky.optString("object")
                if (name.isNotBlank()) {
                    sc.selectAndTrackObjectByName(name)
                }
            }

            // ----- (D) RA/Dec 이동 -----
            if (sky.has("ra") && sky.has("dec")) {
                val ra = sky.optDouble("ra")
                val dec = sky.optDouble("dec")

                // 선택 여부 기준으로 분기
                if (sky.optBoolean("select", false)) {
                    sc.pointMoveToRaDec(ra, dec, sky.optDouble("fovRaDec", 20.0))
                } else {
                    sc.moveToRaDec(ra, dec, sky.optDouble("fovRaDec", 20.0))
                }
            }

            // ----- (F) 줌 처리 -----
            if (sky.has("zoom")) {
                sc.zoomTo(sky.optDouble("zoom"))
            }

            // ----- (E) 토글 설정 -----
            sky.optJSONObject("toggles")?.let { tg ->
                tg.optBoolean("constellation", false).let { sc.toggleConstellations(it) }
                tg.optBoolean("equatorialGrid", false).let { sc.toggleEquatorialGrid(it) }
                tg.optBoolean("azimuthalGrid", false).let { sc.toggleAzimuthalGrid(it) }
                tg.optBoolean("atmosphere", false).let { sc.toggleAtmosphere(it) }
                tg.optBoolean("landscape", false).let { sc.toggleLandscape(it) }
                tg.optBoolean("dsos", false).let { sc.toggleDSOs(it) }
            }
        }
    }

    fun stop() {
        scope.launch {
            runningJob?.cancel()
            timerJob?.cancel()
            _timerRemaining.value = 0L
            _currentStepDuration.value = 0L
            _log.value = "🛑 시나리오 중단됨"
            _state.value = EduState.Ready
        }
    }
}

// ===============================================================
// ✅ EduViewModel
// ===============================================================
@HiltViewModel
class EduViewModel @Inject constructor(
    private val engine: EduEngine
) : ViewModel() {
    private val _viewEduProgram = MutableStateFlow(true)
    val viewEduProgram = _viewEduProgram.asStateFlow()

    val state = engine.state
    val log = engine.log
    val timer = engine.timerRemaining
    val duration = engine.currentStepDuration

    val title = engine.currentTitle
    val programTitle = engine.programTitle
    val sectionIndex = engine.currentSection
    val totalSections = engine.totalSections
    val autoPlay = engine.autoPlay


    fun preloadScenario(context: Context) = viewModelScope.launch {
        engine.loadScenarioWithLoading {
            loadJsonFromAssets(context, "edu/Cygnus.json")
        }
    }

    fun start() = engine.start()
    fun next() = engine.nextStep()
    fun prev() = engine.prevStep()
    fun toggleAuto() = engine.toggleAutoPlay()
    fun stop() = engine.stop()

    // 교육 강제 종료시
    fun closeProgram() = viewModelScope.launch {
        engine.stop()
        Live2DControllerViewModel.chat("교육을 종료할게! 다음에 또 보자 ✨", Emotion.Happy)
        delay(2500)
        Live2DControllerViewModel.playExitMotion()
    }

    // 교육 완료시
    init {
        viewModelScope.launch {
            engine.state.collect { s ->
                if (s is EduState.Ended) handleEnded()
            }
        }
    }

    private fun handleEnded() {
        viewModelScope.launch {
            Live2DControllerViewModel.chat(
                "모든 교육이 끝났어! 함께해줘서 고마워 ✨",
                Emotion.Happy
            )
            delay(3500)
            Live2DControllerViewModel.playExitMotion()
        }
    }
}

// ===============================================================
// ✅ EduProgramScreen
// ===============================================================
@Composable
fun EduProgramScreen() {
    val activity = LocalActivity.current ?: return
    val vm: EduViewModel = hiltViewModel()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            controller.show(WindowInsetsCompat.Type.systemBars())
            vm.stop()
            StellariumController.clearBinding()
        }
    }

    LaunchedEffect(Unit) { vm.preloadScenario(context) }

    Box(Modifier.fillMaxSize()) {
        StellariumScreen(SkyMode.EDUCATION)
        EduOverlayUI()
    }
}

// ===============================================================
// ✅ EduOverlayUI (최대 압축 + VM 내부에서 가져옴)
// ===============================================================
@Composable
fun EduOverlayUI() {
    val vm: EduViewModel = hiltViewModel()

    val programTitle by vm.programTitle.collectAsState()
    val sectionTitle by vm.title.collectAsState()
    val log by vm.log.collectAsState()
    val timer by vm.timer.collectAsState()
    val stepDuration by vm.duration.collectAsState()
    val currentSection by vm.sectionIndex.collectAsState()
    val totalSections by vm.totalSections.collectAsState()
    val autoPlay by vm.autoPlay.collectAsState()
    val state by vm.state.collectAsState()

    Box(Modifier.fillMaxSize()) {

        if (state is EduState.Ready) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { vm.start() }) { Text("교육 시작!") }
            }
        }

        Text(
            "$programTitle - $sectionTitle",
            style = MaterialTheme.typography.titleLarge,
            color = TextHighlight,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { vm.toggleAuto() },
            ) {
                Text(if (autoPlay) "자동 ON" else "자동 OFF")
            }

            Button(onClick = { vm.closeProgram() }) {
                Text("종료")
            }
        }
        // TODO : 테스트 끝나면 log는 삭제
        Text(
            text =
            if (totalSections > 0 && currentSection >= 0)
                "SECTION ${currentSection + 1}/$totalSections · $log"
            else log,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (autoPlay && stepDuration > 0 && timer in 1..stepDuration) {
                val progress = (1f - timer.toFloat() / stepDuration).coerceIn(0f, 1f)
                CircularProgressIndicator(progress = { progress })
            }
            Button(onClick = { vm.prev() }) { Text("이전") }
            Button(onClick = { vm.next() }) { Text("다음") }
        }
    }
}