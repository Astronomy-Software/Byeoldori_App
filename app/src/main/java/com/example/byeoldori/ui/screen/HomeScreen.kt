package com.example.byeoldori.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.ui.screen.MyPage.MyPageScreen
import com.example.byeoldori.ui.screen.Observatory.ObservatoryScreen
import com.example.byeoldori.ui.screen.SkyMap.SkyMapScreen
import com.example.byeoldori.viewmodel.NavigationViewModel
import com.example.byeoldori.viewmodel.Screen
import com.example.byeoldori.ui.screen.Recommended.RecommendedScreen

@Composable
fun HomeScreen() {
    val navViewModel: NavigationViewModel = viewModel()
    val screen by navViewModel.currentScreen.collectAsState()

    when (screen) {
        Screen.SkyMap -> SkyMapScreen(
            onNavigateTo = { navViewModel.navigateTo(it) }
        )
        Screen.Observatory -> ObservatoryScreen(
            onNavigateTo = { navViewModel.navigateTo(Screen.SkyMap) }
        )
        Screen.MyPage -> MyPageScreen(
            onNavigateTo = { navViewModel.navigateTo(Screen.SkyMap) }
        )
        Screen.Recommended -> RecommendedScreen(
            onNavigateTo = { navViewModel.navigateTo(Screen.SkyMap) }
        )
    }
}
