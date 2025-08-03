package com.example.byeoldori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.byeoldori.ui.ByeoldoriApp
import com.example.byeoldori.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                ByeoldoriApp()
            }
        }
    }
}

