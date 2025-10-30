package com.example.byeoldori.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.byeoldori.R
import com.example.byeoldori.skymap.StellariumServer
import com.example.byeoldori.utils.SweObjUtils

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // 미리 초기화 시킬 내용이 있으면 여기로
        // SweObjUtils 초기화
        SweObjUtils.initialize(context = context)
        // 스텔라리움 서버 시작
        StellariumServer.startIfNeeded(context)
        // TODO : Live2D 캐릭터 렌더링 추가
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.byeoldori),
            contentDescription = "앱 로고",
            modifier = Modifier.size(300.dp)
        )
    }
}
