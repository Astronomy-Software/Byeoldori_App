// SkyMapScreen.kt
package com.example.byeoldori.ui.screen.SkyMap

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.ui.screen.Observatory.BottomNavBar
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

    var selectedBottomItem by rememberSaveable { mutableStateOf("별지도") }

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

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedItem = selectedBottomItem,
                onItemSelected = { item ->
                    selectedBottomItem = item
                    when (item) {
                        "홈" -> {}
                        "별지도" -> {} // 현재 화면
                        "관측지" -> navViewModel.navigateTo(AppScreen.Observatory)
                        "커뮤니티" -> {}
                        "마이페이지" -> navViewModel.navigateTo(AppScreen.MyPage)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(gestureModifier)
        ) {
            AndroidView(
                factory = { glView },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
