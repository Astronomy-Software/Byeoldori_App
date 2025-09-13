package com.example.byeoldori.ui.screen.Home

//@Composable
//fun HomeScreen() {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = "홈화면이에요",
//            fontSize = 28.sp // 글자 크기 키움
//        )
//        LoginScreen()
//    }
//}

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.data.AuthRepository
import com.example.byeoldori.data.LoginScreen
import com.example.byeoldori.data.LoginViewModel
import com.example.byeoldori.data.Network

@Composable
fun HomeScreen() {
    // Application 얻기 (팩토리에 필요)
    val app = LocalContext.current.applicationContext as Application

    // ViewModelFactory를 remember로 1회만 생성
    val vmFactory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = AuthRepository(Network.authApi)
                return LoginViewModel(app, repo) as T
            }
        }
    }

    // LoginViewModel 주입
    val loginVm: LoginViewModel = viewModel(factory = vmFactory)

    // 레이아웃: Column으로 타이틀 + 로그인 UI를 세로 배치
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "홈화면이에요",
            fontSize = 28.sp
        )
        Spacer(Modifier.height(16.dp))

        // 로그인 UI 삽입
        LoginScreen(vm = loginVm)
    }
}

