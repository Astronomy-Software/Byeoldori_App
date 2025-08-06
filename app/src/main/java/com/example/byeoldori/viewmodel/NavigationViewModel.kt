package com.example.byeoldori.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NavigationViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow(AppScreen.SkyMap)
    val currentScreen: StateFlow<AppScreen> = _currentScreen

    fun navigateTo(screen: AppScreen ) {
        _currentScreen.value = screen
    }
}

enum class AppScreen  {
    SkyMap,
    Observatory,
    MyPage,
    Recommended,
    LocationRecommend,
}
