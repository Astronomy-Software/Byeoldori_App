//package com.example.byeoldori.eduprogram
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.byeoldori.character.Emotion
//import com.example.byeoldori.character.Live2DControllerViewModel
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class EduViewModel @Inject constructor(
//    private val engine: EduEngine
//) : ViewModel() {
//
//    val isLoading = engine.isLoading
//    val isReady = engine.isReady
//    val isStarted = engine.isStarted
//    val isEnded = engine.isEnded
//
//    val log = engine.log
//    val timer = engine.timerRemaining
//    val title = engine.currentTitle
//    val programTitle = engine.programTitle
//    val sectionIndex = engine.currentSection
//    val totalSections = engine.totalSections
//    val autoPlay = engine.autoPlay
//
//    // ✅ 앱 전체에서 EduProgram을 보이게/숨기게 하는 StateFlow
//    private val _viewEduProgram = MutableStateFlow(true)
//    val viewEduProgram = _viewEduProgram.asStateFlow()
//
//    fun preloadScenario() = viewModelScope.launch {
//        engine.loadScenarioWithLoading { getScenarioJson() }
//    }
//
//    private fun getScenarioJson(): String {
//        return CYGNUS_SCENARIO_JSON
//    }
//
//    fun start() = engine.start()
//    fun next() = engine.nextStep()
//    fun prev() = engine.prevStep()
//    fun toggleAuto() = engine.toggleAutoPlay()
//    fun stop() = engine.stop()
//
//    /** ✅ 종료 버튼: Live2D 멘트 → 퇴장 → EduProgram 닫기 */
//    fun closeProgram() = viewModelScope.launch {
//        engine.stop()
//        Live2DControllerViewModel.chat("교육을 종료할게! 다음에 또 보자 ✨", Emotion.Happy)
//        delay(3000)
//        Live2DControllerViewModel.playExitMotion()
//        delay(1000)
//        _viewEduProgram.value = false
//    }
//}
