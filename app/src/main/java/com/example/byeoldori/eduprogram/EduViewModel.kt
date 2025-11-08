package com.example.byeoldori.eduprogram

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.character.Emotion
import com.example.byeoldori.character.Live2DControllerViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

// ===============================================================
// ✅ EduViewModel
// ===============================================================
@HiltViewModel
class EduViewModel @Inject constructor(
    private val engine: EduEngine,
    private val jsonLoader: JsonLoader
) : ViewModel() {
    // 테스트용 true 아니면 false
    // 테스트용일경우
    private val testMode = false

    private val _viewEduProgram = MutableStateFlow(testMode)
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

    private val _programId = MutableStateFlow<Long?>(null)
    val programId = _programId.asStateFlow()

    private val _programUrl = MutableStateFlow("https://byeoldori-app.duckdns.org/files/json/2025/11/08/8b05875c0aca4497817881dce9d1ae44.json")
    val programUrl = _programUrl.asStateFlow()

    fun openProgram(programId: Long, url: String) {
        _programId.value = programId
        _programUrl.value = url
        _viewEduProgram.value = true
        println("✅ 교육 프로그램 열림: id=$programId, url=$url")
    }

    fun preloadScenario(context: Context) = viewModelScope.launch {
        val json: JSONObject? = if (testMode) {
            // ✅ 테스트 모드: assets에서 로드
            withContext(Dispatchers.IO) {
                val jsonText = context.assets.open("edu/test.json")
                    .bufferedReader().use { it.readText() }
                JSONObject(jsonText)
            }
        } else {
            val url = _programUrl.value
            jsonLoader.loadFromUrl( url )
        }

        // ✅ 로드 결과 전달
        engine.loadScenarioWithLoading { json }
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
        Live2DControllerViewModel.playExitMotion()
        delay(3000)
        eduClose()
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

    fun eduClose(){
        _viewEduProgram.value = false
    }

    fun resetStateOnlyAndRestart() = engine.resetStateOnlyAndRestart()
}
