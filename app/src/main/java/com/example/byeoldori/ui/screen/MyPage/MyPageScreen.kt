package com.example.byeoldori.ui.screen.MyPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Purple800
import com.example.byeoldori.viewmodel.AppScreen
import com.example.byeoldori.viewmodel.NavigationViewModel

@Preview(showBackground = true)
@Composable
fun MyPageScreen() {
    val navViewModel: NavigationViewModel = viewModel()
        Button(onClick = {navViewModel.navigateTo(AppScreen.SkyMap)}) {
            Text("To 별지도")
        }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)  // 바깥 여백
//    ) {
//        WideButton(
//            text = "일반 로그인",
//            onClick = { /* TODO */ }
//        )
//        WideButton(
//            text = "일반 로그인",
//            onClick = { /* TODO */ }
//        )
//        WideButton(
//            text = "일반 로그인",
//            onClick = { /* TODO */ }
//        )
//    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2열 고정
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(10) { index ->
            Column (
                modifier = Modifier
                    .aspectRatio(1f) // 정사각형
                    .fillMaxWidth()
                    .background(Purple800),
            ) {
                Text("asdf")
                Text("text")
                WideButton(
                    text = "일반 ",
                    onClick = { /* TODO */ }
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("타이틀", style = MaterialTheme.typography.titleLarge)
                        Text("본문 내용", style = MaterialTheme.typography.bodyMedium)
                    }
                }

            }
        }
    }
}

