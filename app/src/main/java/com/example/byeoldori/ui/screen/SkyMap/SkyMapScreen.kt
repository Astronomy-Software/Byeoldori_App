// SkyMapScreen.kt
package com.example.byeoldori.ui.screen.SkyMap

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.ui.screen.SkyMap.render.CelestialGLView
import com.example.byeoldori.viewmodel.NavigationViewModel
import com.example.byeoldori.viewmodel.AppScreen
import com.example.byeoldori.viewmodel.Skymap.CameraViewModel

@Composable
fun SkyMapScreen() {
    val context = LocalContext.current
    val navViewModel: NavigationViewModel = viewModel()
    val camViewModel: CameraViewModel = viewModel()

    val yaw by camViewModel.yaw.collectAsState()
    val pitch by camViewModel.pitch.collectAsState()
    val fov by camViewModel.fov.collectAsState()
    val isAuto by camViewModel.isAutoMode.collectAsState()

    val glView = remember { CelestialGLView(context) }
    LaunchedEffect(yaw, pitch, fov) {
        glView.renderer.updateCamera(yaw, pitch, fov)
    }

    // Gesture Modifier: 항상 활성, 자동 모드일 때는 pan 무시하고 pinch만
    val gestureModifier = Modifier.pointerInput(isAuto) {
        detectTransformGestures { _, pan, zoomFactor, _ ->
            if (zoomFactor != 1f) {
                camViewModel.pinchZoom(zoomFactor)
            } else if (!isAuto) {
                camViewModel.updateYaw(pan.x * 0.5f)
                camViewModel.updatePitch(-pan.y * 0.5f)
            }
        }
    }

    // Sensor setup
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val rotationSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    val rotationMatrix = remember { FloatArray(9) }
    val orientationAngles = remember { FloatArray(3) }

    val sensorListener = remember(isAuto) {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!isAuto) return
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val remapped = FloatArray(9)
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remapped
                )
                SensorManager.getOrientation(remapped, orientationAngles)
                camViewModel.setDeviceOrientation(
                    azimuthRad = orientationAngles[0],
                    pitchRad = orientationAngles[1]
                )
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(isAuto) {
        if (isAuto) {
            sensorManager.registerListener(
                sensorListener,
                rotationSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            sensorManager.unregisterListener(sensorListener)
        }
        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    Scaffold { padding ->
        // 항상 제스처 적용
        val boxModifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .then(gestureModifier)

        Box(modifier = boxModifier) {
            AndroidView(factory = { glView }, modifier = Modifier.fillMaxSize())

            Column(modifier = Modifier.padding(16.dp)) {
                Button(onClick = { camViewModel.toggleAutoMode() }) {
                    Text(if (isAuto) "수동 모드" else "자동 모드")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = { navViewModel.navigateTo(AppScreen.Observatory) }) { Text("관측지") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { navViewModel.navigateTo(AppScreen.MyPage) }) { Text("MyPage") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { navViewModel.navigateTo(AppScreen.Recommended) }) { Text("Recommend") }
                }
            }
        }
    }
}
