package com.example.byeoldori.eduprogram

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.example.byeoldori.skymap.SkyMode
import com.example.byeoldori.skymap.StellariumScreen

@Composable
fun EduProgramScreen() {

    Box{
        // TODO : 오버레이 추가
        StellariumScreen(SkyMode.EDUCATION) // TODO : Education 용 설정 추가
    }
}