package com.example.byeoldori.skymap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 📸 SkyCameraTracker
 * - yaw, pitch, fov 상태를 관리하고 변경 시 콜백 전달
 * - StellariumWebController와 독립적으로 동작
 */
class SkyCameraTracker {

    private val _yaw = MutableStateFlow(0f)
    private val _pitch = MutableStateFlow(0f)
    private val _fov = MutableStateFlow(60f)

    val yaw: StateFlow<Float> get() = _yaw
    val pitch: StateFlow<Float> get() = _pitch
    val fov: StateFlow<Float> get() = _fov

    /** 카메라 상태 업데이트 */
    fun updateCamera(newYaw: Float, newPitch: Float, newFov: Float) {
        _yaw.value = newYaw
        _pitch.value = newPitch
        _fov.value = newFov
    }

    /** Stellarium Controller에 동기화 */
    fun bindToStellarium(controller: StellariumWebController) {
        CoroutineScope(Dispatchers.Main).launch {
            launch {
                yaw.collect {
                    controller.setViewDirection(it.toDouble(), pitch.value.toDouble())
                }
            }
            launch {
                pitch.collect {
                    controller.setViewDirection(yaw.value.toDouble(), it.toDouble())
                }
            }
            launch {
                fov.collect {
                    controller.setFov(it.toDouble())
                }
            }
        }
    }
}
