package com.example.byeoldori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.byeoldori.ui.AppEntry
import com.example.byeoldori.ui.theme.AppTheme
import com.example.byeoldori.ui.theme.SystemBars
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                SystemBars() // SystemBar 세팅 적용
                AppEntry()
            }
        }
    }
}

