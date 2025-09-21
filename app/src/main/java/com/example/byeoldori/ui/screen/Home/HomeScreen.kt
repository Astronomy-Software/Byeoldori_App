package com.example.byeoldori.ui.screen.Home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.data.Test2LoginScreen
import com.example.byeoldori.data.TestUserScreen
import com.example.byeoldori.ui.screen.TestObservationSiteScreen

@Composable
fun HomeScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("홈화면이에요", fontSize = 28.sp)
            Spacer(Modifier.height(16.dp))
        }

        item {
            Test2LoginScreen()
        }

        // 관측지 리스트
        item {
            TestObservationSiteScreen()
        }

        item {
            TestUserScreen()
        }
    }
}

