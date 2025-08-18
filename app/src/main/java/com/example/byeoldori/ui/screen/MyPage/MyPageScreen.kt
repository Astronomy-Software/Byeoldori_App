package com.example.byeoldori.ui.screen.MyPage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.ui.screen.Observatory.BottomNavBar
import com.example.byeoldori.viewmodel.AppScreen
import com.example.byeoldori.viewmodel.NavigationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen() {
    val navViewModel: NavigationViewModel = viewModel()
    var selectedBottomItem: String by rememberSaveable { mutableStateOf("마이페이지") }



    Scaffold(
        topBar = {
            TopAppBar(title = { Text("👤 마이페이지") })
        },
        bottomBar = {
            BottomNavBar(
                selectedItem = selectedBottomItem,
                onItemSelected = { item ->
                    selectedBottomItem = item
                    when (item) {
                        "홈" -> {}
                        "별지도" -> navViewModel.navigateTo(AppScreen.SkyMap)
                        "관측지" -> navViewModel.navigateTo(AppScreen.Observatory)
                        "커뮤니티" -> {}
                        "마이페이지" -> {} // 현재 화면
                    }
                }
            )
        }

    ) { padding ->
        Text("마이페이지", modifier = Modifier.padding(16.dp))
    }
}
