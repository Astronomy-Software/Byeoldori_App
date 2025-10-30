package com.example.byeoldori.skymap

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 🌌 SkyCameraController
 * - 자이로센서를 통해 Stellarium 시야(yaw, pitch, fov) 제어
 * - 센서 → 상태(Flow) → WebController 동기화
 */
class SkyCameraController(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : SensorEventListener {

    // ✅ 상태 관리
    private val _yaw = MutableStateFlow(0f)
    private val _pitch = MutableStateFlow(0f)
    private val _fov = MutableStateFlow(60f)

    val yaw: StateFlow<Float> get() = _yaw
    val pitch: StateFlow<Float> get() = _pitch
    val fov: StateFlow<Float> get() = _fov

    // ✅ 센서 관련
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationMatrix = FloatArray(9)
    private val remapMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var isRunning = false
    private var lastUpdate = 0L

    private val DEG = (180.0 / Math.PI).toFloat()

    // =========================
    // 센서 제어
    // =========================
    fun start() {
        if (isRunning) return
        isRunning = true
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) ?: return
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        sensorManager.unregisterListener(this)
    }

    // =========================
    // 카메라 제어 로직
    // =========================
    private fun updateCamera(newYaw: Float, newPitch: Float, newFov: Float) {
        _yaw.value = newYaw
        _pitch.value = newPitch
        _fov.value = newFov
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        val now = System.currentTimeMillis()
        if (now - lastUpdate < 16) return // 60fps 제한
        lastUpdate = now

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            remapMatrix
        )
        SensorManager.getOrientation(remapMatrix, orientationAngles)

        var azimuth = orientationAngles[0] * DEG
        val altitude = -orientationAngles[1] * DEG
        if (azimuth < 0) azimuth += 360f

        scope.launch(Dispatchers.Main.immediate) {
            updateCamera(azimuth, altitude, _fov.value)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // =========================
    // Stellarium 동기화
    // =========================
    fun bindToStellarium(controller: StellariumWebController) {
        scope.launch(Dispatchers.Main) {
            combine(yaw, pitch) { y, p -> y to p }
                .collect { (y, p) -> controller.setViewDirection(y.toDouble(), p.toDouble()) }
        }
        scope.launch(Dispatchers.Main) {
            fov.collect { controller.setFov(it.toDouble()) }
        }
    }
}
