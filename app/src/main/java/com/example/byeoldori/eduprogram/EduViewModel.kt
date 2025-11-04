//package com.example.byeoldori.eduprogram
//
//import android.content.Context
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.byeoldori.skymap.StellariumController
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import org.json.JSONObject
//import java.io.InputStream
//import javax.inject.Inject
//
//@HiltViewModel
//class EduViewModel @Inject constructor(
//    private val engine: EduEngine
//) : ViewModel() {
//
//    // 📘 교육 프로그램 실행 여부
//    private val _viewEduProgram = MutableStateFlow(false)
//    val viewEduProgram = _viewEduProgram.asStateFlow()
//
//    // 🎓 로그 출력용
//    val log = engine.log
//
//    // ⚙️ 초기 세팅 여부
//    private val _initialized = MutableStateFlow(false)
//    val initialized = _initialized.asStateFlow()
//
//    /**
//     * ✅ JSON 파일 로드 및 초기화
//     * - init 섹션을 읽어 초기 세팅 진행
//     */
//    fun loadAndInitialize(context: Context) {
//        viewModelScope.launch {
//            try {
//                val jsonString = context.assets.open("edu_scenario.json")
//                    .bufferedReader().use { it.readText() }
//
//                val root = JSONObject(jsonString)
//                val initConfig = root.optJSONObject("init")
//                val scenarioArray = root.optJSONArray("scenario")
//
//                // 초기화 (init 섹션 기반)
//                engine.initialize(StellariumController, initConfig)
//                _initialized.value = true
//
//                // 시나리오 실행 시작
//                scenarioArray?.let {
//                    engine.runScenarioArray(it)
//                }
//
//            } catch (e: Exception) {
//                println("❌ EduViewModel 초기화 오류: ${e.message}")
//            }
//        }
//    }
//
//    /**
//     * ▶️ 프로그램 수동 시작 (외부 호출용)
//     */
//    fun startProgram(jsonStream: InputStream) {
//        viewModelScope.launch {
////            engine.runScenario(jsonStream)
//        }
//    }
//
//    /**
//     * ⏹️ 시나리오 중단
//     */
//    fun stopProgram() {
//        engine.stop()
//    }
//
//    fun setViewEduProgram(value: Boolean) {
//        _viewEduProgram.value = value
//    }
//}
