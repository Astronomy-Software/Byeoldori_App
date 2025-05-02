package com.example.byeoldori

import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.byeoldori.ui.ByeoldoriApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Compose 시작
        setContent {
            ByeoldoriApp()
        }
    }
}

