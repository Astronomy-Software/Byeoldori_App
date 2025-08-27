package com.example.byeoldori.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.ui.screen.MyPage.MyPageScreen
import com.example.byeoldori.ui.screen.Observatory.ObservatoryScreen
import com.example.byeoldori.ui.screen.SkyMap.SkyMapScreen
import com.example.byeoldori.viewmodel.NavigationViewModel
import com.example.byeoldori.viewmodel.*
import com.example.byeoldori.ui.screen.Recommended.RecommendedScreen

@Composable
fun HomeScreen() {
    val navViewModel: NavigationViewModel = viewModel()
    val screen by navViewModel.currentScreen.collectAsState()

    when (screen) {
        AppScreen.SkyMap -> SkyMapScreen()
        AppScreen.Observatory -> ObservatoryScreen()
        AppScreen.MyPage -> MyPageScreen()
        AppScreen.Recommended -> RecommendedScreen()
    }
}
