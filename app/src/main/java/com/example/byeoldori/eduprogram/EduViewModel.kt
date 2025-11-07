package com.example.byeoldori.eduprogram

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.character.Emotion
import com.example.byeoldori.character.Live2DControllerViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
