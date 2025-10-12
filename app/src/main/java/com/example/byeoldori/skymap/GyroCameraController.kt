package com.example.byeoldori.skymap

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.lang.Math.toDegrees

class GyroCameraController(
    private val context: Context,
    private val tracker: SkyCameraTracker
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationMatrix = FloatArray(9)
    private val remapMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        // 1) Rotation Vector → 회전 행렬
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        // 2) 카메라 방향에 맞게 좌표 remap
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            remapMatrix
        )

        // 3) Orientation 추출
        SensorManager.getOrientation(remapMatrix, orientationAngles)

        var azimuthDeg = toDegrees(orientationAngles[0].toDouble()).toFloat()
        val altitudeDeg = -toDegrees(orientationAngles[1].toDouble()).toFloat()

        // 4) 방위각 0~360도 정규화
        if (azimuthDeg < 0) azimuthDeg += 360f

        // 5) 결과 출력 및 반영
        Log.d(
            "GyroController",
            "Camera Azimuth=${"%.2f".format(azimuthDeg)}°, Altitude=${"%.2f".format(altitudeDeg)}°"
        )

        tracker.updateCamera(
            azimuthDeg,
            altitudeDeg,
            tracker.fov.value
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
