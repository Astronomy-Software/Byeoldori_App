package com.example.byeoldori.character

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class Live2DControllerViewModel @Inject constructor(
    val controller: Live2DController
) : ViewModel() {
    //TODO : 마스코트 등장, 퇴장 app screen에서 기타등등 좀더 사용하기쉬운 메소드들 추가
}
