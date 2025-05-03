package com.example.byeoldori.ui.screen.SkyMap

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.viewmodel.AppScreen
import com.example.byeoldori.viewmodel.NavigationViewModel

@Composable
fun SkyMapScreen() {
    val navViewModel: NavigationViewModel = viewModel()

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).padding(32.dp)) {
            Button(onClick = { navViewModel.navigateTo(AppScreen.Observatory) }) {
                Text("Observatory")
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

