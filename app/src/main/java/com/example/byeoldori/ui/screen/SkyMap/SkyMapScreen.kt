// SkyMapScreen.kt
package com.example.byeoldori.ui.screen.SkyMap

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.viewmodel.AppScreen
import com.example.byeoldori.viewmodel.NavigationViewModel
import com.example.byeoldori.viewmodel.Skymap.CameraViewModel
import com.example.byeoldori.ui.screen.SkyMap.render.CelestialGLView

@Composable
fun SkyMapScreen() {
    val navViewModel: NavigationViewModel = viewModel()
    val camViewModel: CameraViewModel = viewModel()
    val context = LocalContext.current

    val glView = remember { CelestialGLView(context) }

    val yaw by camViewModel.yaw.collectAsState()
    val pitch by camViewModel.pitch.collectAsState()
    val fov by camViewModel.fov.collectAsState()

    LaunchedEffect(yaw, pitch, fov) {
        glView.renderer.updateCamera(yaw, pitch, fov)
    }

    val gestureModifier = Modifier.pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
            camViewModel.updateYaw(pan.x * 0.5f)
            camViewModel.updatePitch(-pan.y * 0.5f)

            if (zoom != 1f) {
                val delta = (1f - zoom) * 30f
                camViewModel.zoom(delta)
            }
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .then(gestureModifier)) {

            AndroidView(
                factory = { glView },
                modifier = Modifier.fillMaxSize()
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Button(onClick = { navViewModel.navigateTo(AppScreen.Observatory) }) {
                    Text("관측지로")
                }
                Button(onClick = { navViewModel.navigateTo(AppScreen.MyPage) }) {
                    Text("MyPage")
                }
                Button(onClick = { navViewModel.navigateTo(AppScreen.Recommended) }) {
                    Text("Recommend")
                }
            }
        }
    }
}
