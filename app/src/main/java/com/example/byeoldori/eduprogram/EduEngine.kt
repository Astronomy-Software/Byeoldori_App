package com.example.byeoldori.eduprogram

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

class EduEngine @Inject constructor() {

    private var scenarioJob: Job? = null
    private val _log = MutableStateFlow("")
    val log = _log.asStateFlow()

    suspend fun runScenario(inputStream: InputStream) {
        stop()
        scenarioJob = CoroutineScope(Dispatchers.Default).launch {
            val json = JSONArray(inputStream.bufferedReader().use { it.readText() })
            for (i in 0 until json.length()) {
                ensureActive()
                val step = json.getJSONObject(i)
                val action = step.getString("action")
                _log.value = "실행 중: $action"
                delay(1000)
            }
            _log.value = "시나리오 완료"
        }
    }

    fun stop() {
        scenarioJob?.cancel()
        _log.value = "중단됨"
    }
}
