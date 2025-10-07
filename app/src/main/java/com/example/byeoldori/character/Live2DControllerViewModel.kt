package com.example.byeoldori.character

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class Live2DControllerViewModel @Inject constructor(
    val controller: Live2DController
) : ViewModel()
