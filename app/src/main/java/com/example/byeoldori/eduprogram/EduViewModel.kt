package com.example.byeoldori.eduprogram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EduViewModel @Inject constructor(
    private val engine: EduEngine
) : ViewModel() {

    private val _viewEduProgram = MutableStateFlow(false)
    val viewEduProgram = _viewEduProgram.asStateFlow()

    fun setViewEduProgram(value: Boolean) {
        _viewEduProgram.value = value
    }

    val log = engine.log

    fun startProgram(jsonStream: java.io.InputStream) {
        viewModelScope.launch {
            engine.runScenario(jsonStream)
        }
    }

    fun stopProgram() {
        engine.stop()
    }
}
