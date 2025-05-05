package com.example.byeoldori.viewmodel.Skymap

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CameraViewModel : ViewModel() {
    private val _yaw = MutableStateFlow(0f)
    val yaw: StateFlow<Float> = _yaw

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch

    private val _fov = MutableStateFlow(60f)
    val fov: StateFlow<Float> = _fov

    fun updateYaw(delta: Float) {
        _yaw.value += delta
    }

    fun updatePitch(delta: Float) {
        _pitch.value = (_pitch.value + delta).coerceIn(-90f, 90f)
    }

    fun zoom(delta: Float) {
        val sensitivity = _fov.value / 60f
        val adjusted = delta * sensitivity
        _fov.value = (_fov.value + adjusted).coerceIn(5f, 45f)
    }

    fun setCamera(yaw: Float, pitch: Float, fov: Float) {
        _yaw.value = yaw
        _pitch.value = pitch
        _fov.value = fov.coerceIn(30f, 90f)
    }
}
