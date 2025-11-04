package com.example.byeoldori.eduprogram

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.character.Live2DController
import com.example.byeoldori.character.Live2DTestUI
import com.example.byeoldori.character.TailPosition
import com.example.byeoldori.skymap.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * ===============================================================
 * 🎓 EduProgram v3.2 (Circular Timer & Long Duration)
 * - 타이머를 둥근 원형으로 표시
 * - 각 step의 duration을 여유롭게 확장 (약 1.5~2배)
 * - delay() 완전 제거
 * ===============================================================
 */

// 🌌 백조자리(Cygnus) 시나리오
private const val CYGNUS_SCENARIO_JSON = """
{
  "init": {
    "mode": "education",
    "fov": 70,
    "view": { "yaw": 60, "pitch": 25 },
    "toggles": {
      "constellation": false,
      "equatorialGrid": false,
      "azimuthalGrid": false,
      "atmosphere": true,
      "landscape": true
    }
  },
  "scenario": [
    {
      "title": "백조자리 들어가기",
      "desc": "여름철 은하수를 가로지르는 대표 별자리, 백조자리(Cygnus)를 살펴본다.",
      "autoDelay": true,
      "step": [
        { "action": "speak", "params": { "text": "안녕! 오늘은 여름밤 은하수 한가운데를 가로지르는 아름다운 별자리, 백조자리(Cygnus)를 볼 거야." }, "duration": 4800 },
        { "action": "move_camera", "params": { "yaw": 60, "pitch": 20 }, "duration": 2000 }
      ]
    },
    {
      "title": "백조자리 전체 보기",
      "desc": "별자리 전체 윤곽을 먼저 본다.",
      "autoDelay": false,
      "step": [
        { "action": "speak", "params": { "text": "백조자리는 실제 하늘에서 십자가 모양으로 보이기도 하고, 은하수 위를 날아가는 새처럼 보이기도 해." }, "duration": 5000 },
        { "action": "show_object", "params": { "name": "NAME Cygnus" }, "duration": 4000 },
        { "action": "show_constellation", "params": { "visible": true }, "duration": 2000 },
        { "action": "speak", "params": { "text": "지금 표시된 게 백조자리 전체야. 가운데가 몸통, 양옆이 날개, 위쪽이 꼬리 방향이야." }, "duration": 4500 }
      ]
    },
    {
      "title": "꼬리별 데네브",
      "desc": "백조자리에서 가장 밝은 별, 꼬리 쪽의 데네브(Deneb).",
      "autoDelay": true,
      "step": [
        { "action": "speak", "params": { "text": "먼저 백조자리의 꼬리 끝에 있는 데네브(Deneb)를 볼까?" }, "duration": 4800 },
        { "action": "show_object", "params": { "name": "NAME Deneb" }, "duration": 4000 },
        { "action": "speak", "params": { "text": "데네브는 아랍어로 '꼬리'라는 뜻이야. 백조가 은하수를 거슬러 날아가는 꼬리 부분이 바로 이 별이지." }, "duration": 6000 }
      ]
    },
    {
      "title": "몸통의 사드르",
      "desc": "백조의 중심, 감마별 사드르(Sadr)를 본다.",
      "autoDelay": true,
      "step": [
        { "action": "speak", "params": { "text": "이제 몸통 중앙으로 가보자. 데네브에서 내려오면 사드르(Sadr)가 있어." }, "duration": 4800 },
        { "action": "show_object", "params": { "name": "NAME Sadr" }, "duration": 3500 },
        { "action": "speak", "params": { "text": "사드르는 백조의 가슴 부분이야. 이곳에서 날개가 좌우로 펼쳐져 있지." }, "duration": 5000 }
      ]
    },
    {
      "title": "부리의 알비레오",
      "desc": "백조자리의 부리 끝, 이중성 알비레오(Albireo).",
      "autoDelay": false,
      "step": [
        { "action": "speak", "params": { "text": "몸통 끝까지 내려가면 부리 부분에 알비레오(Albireo)가 있어." }, "duration": 4800 },
        { "action": "show_object", "params": { "name": "NAME Albireo" }, "duration": 4000 },
        { "action": "speak", "params": { "text": "알비레오는 작은 망원경으로 보면 파란별과 노란별이 나란히 있는 아름다운 이중성이야." }, "duration": 5500 }
      ]
    },
    {
      "title": "날개 부분 보기",
      "desc": "사드르를 중심으로 좌우로 펼쳐진 날개.",
      "autoDelay": true,
      "step": [
        { "action": "speak", "params": { "text": "사드르를 중심으로 양쪽으로 뻗은 별줄이 백조의 날개야." }, "duration": 5000 },
        { "action": "move_camera", "params": { "yaw": 65, "pitch": 22 }, "duration": 2000 },
        { "action": "move_camera", "params": { "yaw": 55, "pitch": 22 }, "duration": 2000 },
        { "action": "speak", "params": { "text": "백조가 은하수 위를 날개 펴고 날아가는 모습, 상상이 되지?" }, "duration": 5000 }
      ]
    },
    {
      "title": "은하수 위의 백조",
      "desc": "은하수를 따라 길게 뻗은 백조자리.",
      "autoDelay": true,
      "step": [
        { "action": "toggle_atmosphere", "params": { "visible": true }, "duration": 1000 },
        { "action": "speak", "params": { "text": "백조자리는 은하수를 따라 길게 놓여 있어서, 마치 하늘 위를 유영하는 새처럼 보여." }, "duration": 5500 },
        { "action": "speak", "params": { "text": "그래서 여름밤 하늘에서 백조자리를 찾으면, 은하수도 자연스럽게 함께 볼 수 있단다." }, "duration": 5000 }
      ]
    },
    {
      "title": "여름철 대삼각형",
      "desc": "데네브, 베가, 알타이르로 이어지는 대삼각형.",
      "autoDelay": false,
      "step": [
        { "action": "speak", "params": { "text": "이제 데네브를 중심으로 여름철 대삼각형을 연결해볼까?" }, "duration": 4800 },
        { "action": "show_object", "params": { "name": "NAME Deneb" }, "duration": 2000 },
        { "action": "unlock_view" },
        { "action": "show_object", "params": { "name": "NAME Vega" }, "duration": 3000 },
        { "action": "show_object", "params": { "name": "NAME Altair" }, "duration": 3000 },
        { "action": "speak", "params": { "text": "데네브는 꼬리별이면서 여름철 대삼각형의 한 꼭짓점이야." }, "duration": 5500 }
      ]
    },
    {
      "title": "백조자리의 신화",
      "desc": "백조자리에 얽힌 전설로 마무리.",
      "autoDelay": true,
      "step": [
        { "action": "speak", "params": { "text": "백조자리는 제우스가 변신한 모습으로도, 음악가 오르페우스가 된 별로도 전해져." }, "duration": 6000 },
        { "action": "speak", "params": { "text": "이야기를 알고 하늘을 보면, 단순한 점들의 모임이 아니라 생생한 이야기로 보이게 돼." }, "duration": 5000 },
        { "action": "speak", "params": { "text": "이제 너도 하늘에서 백조를 찾아볼 수 있겠지?" }, "duration": 4000 }
      ]
    }
  ]
}
"""

// ===============================================================
// 🎬 EduEngine
// ===============================================================
class EduEngine @Inject constructor(
    private val live2D: Live2DController
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var scenarioArray: JSONArray? = null
    private var currentIndex = -1
    private var runningJob: Job? = null
    private var timerJob: Job? = null
    private var lastLockedObject: String? = null

    private val _log = MutableStateFlow("엔진 대기 중")
    val log = _log.asStateFlow()

    private val _totalSteps = MutableStateFlow(0)
    val totalSteps = _totalSteps.asStateFlow()

    private val _currentStep = MutableStateFlow(-1)
    val currentStep = _currentStep.asStateFlow()

    private val _currentTitle = MutableStateFlow("")
    val currentTitle = _currentTitle.asStateFlow()

    private val _currentDesc = MutableStateFlow("")
    val currentDesc = _currentDesc.asStateFlow()

    private val _timerRemaining = MutableStateFlow(0L)
    val timerRemaining = _timerRemaining.asStateFlow()

    fun stop() {
        scope.launch {
            runningJob?.cancelAndJoin()
            timerJob?.cancelAndJoin()
            _timerRemaining.value = 0L
            _log.value = "🛑 시나리오 중단됨"
        }
    }

    fun loadScenario(root: JSONObject) {
        stop()
        initialize(root.optJSONObject("init"))
        val arr = root.optJSONArray("scenario") ?: JSONArray()
        scenarioArray = arr
        _totalSteps.value = arr.length()
        _currentStep.value = -1
        currentIndex = -1
        _log.value = "✅ 시나리오 로드 완료 (총 ${arr.length()} 단계)"
    }

    private fun initialize(init: JSONObject?) {
        val sc = StellariumController
        if (init?.optString("mode") == "education") sc.setEducationMode()
        sc.setFov(init?.optDouble("fov", 70.0) ?: 70.0)
        init?.optJSONObject("view")?.let {
            sc.setViewDirection(it.optDouble("yaw", 180.0), it.optDouble("pitch", 25.0))
        }
        init?.optJSONObject("toggles")?.let {
            sc.toggleConstellations(it.optBoolean("constellation", true))
            sc.toggleEquatorialGrid(it.optBoolean("equatorialGrid", false))
            sc.toggleAzimuthalGrid(it.optBoolean("azimuthalGrid", false))
            sc.toggleAtmosphere(it.optBoolean("atmosphere", true))
            sc.toggleLandscape(it.optBoolean("landscape", true))
        }
    }

    fun nextStep() {
        val arr = scenarioArray ?: return
        val next = (currentIndex + 1).coerceAtMost(arr.length() - 1)
        runStep(next)
    }

    fun prevStep() {
        val arr = scenarioArray ?: return
        val prev = (currentIndex - 1).coerceAtLeast(0)
        runStep(prev)
    }

    private fun runStep(index: Int) {
        val arr = scenarioArray ?: return
        if (index < 0 || index >= arr.length()) return

        scope.launch {
            runningJob?.cancelAndJoin()
            timerJob?.cancelAndJoin()
            _timerRemaining.value = 0L

            runningJob = launch {
                val stepObj = arr.getJSONObject(index)
                val actions = stepObj.optJSONArray("step") ?: JSONArray()
                val autoDelay = stepObj.optBoolean("autoDelay", false)

                _currentTitle.value = stepObj.optString("title", "STEP ${index + 1}")
                _currentDesc.value = stepObj.optString("desc", "")

                for (i in 0 until actions.length()) {
                    ensureActive()
                    val actionObj = actions.getJSONObject(i)
                    runSingleAction(actionObj)
                    val duration = actionObj.optLong("duration", 0L)
                    if (duration > 0) startTimer(duration) {}
                }

                _currentStep.value = index
                currentIndex = index

                if (autoDelay && isActive) startTimer(4000L) { nextStep() }
            }
        }
    }

    private fun startTimer(durationMs: Long, onFinished: () -> Unit) {
        timerJob?.cancel()
        timerJob = scope.launch {
            val start = System.currentTimeMillis()
            val end = start + durationMs
            while (isActive && System.currentTimeMillis() < end) {
                val remaining = end - System.currentTimeMillis()
                _timerRemaining.value = remaining
                _log.value = "⏳ 남은 시간: ${"%.1f".format(remaining / 1000.0)}초"
                yield()
                delay(80L)
            }
            if (isActive) {
                _timerRemaining.value = 0L
                onFinished()
            }
        }
    }

    private suspend fun runSingleAction(actionObj: JSONObject) {
        val action = actionObj.optString("action")
        val params = actionObj.optJSONObject("params")

        when (action) {
            "speak" -> {
                val text = params?.optString("text") ?: ""
                _log.value = "💬 별도리: $text"
                live2D.showCharacter()
                live2D.showSpeech(text, TailPosition.Left, Alignment.TopCenter)
            }
            "move_camera" -> {
                if (lastLockedObject != null) return
                StellariumController.setViewDirection(
                    params?.optDouble("yaw", 180.0) ?: 180.0,
                    params?.optDouble("pitch", 25.0) ?: 25.0
                )
            }
            "set_fov" -> {
                if (lastLockedObject != null) return
                StellariumController.setFov(params?.optDouble("fov", 70.0) ?: 70.0)
            }
            "show_object" -> {
                val name = params?.optString("name") ?: ""
                if (name.isNotBlank()) {
                    lastLockedObject = name
                    StellariumController.setLookUpObject("\"$name\"")
                    _log.value = "🔭 대상 선택: $name"
                }
            }
            "unlock_view" -> {
                lastLockedObject = null
                _log.value = "🔓 시선 고정 해제"
            }
            "toggle_atmosphere" ->
                StellariumController.toggleAtmosphere(params?.optBoolean("visible", false) ?: false)
            "show_constellation" ->
                StellariumController.toggleConstellations(params?.optBoolean("visible", true) ?: true)
            else -> _log.value = "⚠️ 알 수 없는 명령: $action"
        }
    }
}

// ===============================================================
// 🎓 EduViewModel
// ===============================================================
@HiltViewModel
class EduViewModel @Inject constructor(
    private val engine: EduEngine
) : androidx.lifecycle.ViewModel() {
    val log = engine.log
    val currentStep = engine.currentStep
    val totalSteps = engine.totalSteps
    val title = engine.currentTitle
    val desc = engine.currentDesc
    val timer = engine.timerRemaining

    fun loadScenario() = viewModelScope.launch {
        val root = JSONObject(CYGNUS_SCENARIO_JSON)
        engine.loadScenario(root)
        engine.nextStep()
    }

    fun next() = engine.nextStep()
    fun prev() = engine.prevStep()
    fun stop() = engine.stop()
}

// ===============================================================
// 💬 EduOverlayUI
// ===============================================================
@Composable
fun EduOverlayUI(
    title: String,
    desc: String,
    log: String,
    timer: Long,
    currentStep: Int,
    totalSteps: Int,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        // 제목 & 설명
        Column(Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (desc.isNotEmpty())
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }

        // 진행 로그
        Text(
            text = if (totalSteps > 0)
                "STEP ${if (currentStep >= 0) currentStep + 1 else 0}/$totalSteps · $log"
            else log,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)
        )

        // 둥근 타이머
        if (timer > 0) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { (timer / 5000f).coerceIn(0f, 1f) },
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 6.dp,
                    trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                )
            }
        }

        // 이전 / 다음 / 종료
        Row(
            Modifier.align(Alignment.BottomCenter).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onPrevClick, Modifier.padding(end = 12.dp)) { Text("이전") }
            Button(onClick = onNextClick, Modifier.padding(start = 12.dp)) { Text("다음") }
        }
        Button(
            onClick = onStopClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).height(32.dp)
        ) { Text("종료") }
    }
}

// ===============================================================
// 🌌 EduProgramScreen
// ===============================================================
@Composable
fun EduProgramScreen() {
    val activity = LocalContext.current as Activity
    val window = activity.window
    val vm: EduViewModel = hiltViewModel()

    val log by vm.log.collectAsState()
    val step by vm.currentStep.collectAsState()
    val total by vm.totalSteps.collectAsState()
    val title by vm.title.collectAsState()
    val desc by vm.desc.collectAsState()
    val timer by vm.timer.collectAsState()

    DisposableEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val c = WindowInsetsControllerCompat(window, window.decorView)
        c.hide(WindowInsetsCompat.Type.systemBars())
        c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            c.show(WindowInsetsCompat.Type.systemBars())
            vm.stop()
            StellariumController.clearBinding()
        }
    }

    Box(Modifier.fillMaxSize()) {
        StellariumScreen(SkyMode.EDUCATION)
        EduOverlayUI(title, desc, log, timer, step, total, { vm.prev() }, { vm.next() }, { vm.stop() })
        Live2DTestUI()
    }

    LaunchedEffect(Unit) { vm.loadScenario() }
}