package com.example.byeoldori.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Live2DControllerViewModel @Inject constructor(
    val controller: Live2DController
) : ViewModel() {
    //TODO : 마스코트 등장, 퇴장 app screen에서 기타등등 좀더 사용하기쉬운 메소드들 추가
    fun playIdleMotion() {
        controller.playMotion("Idle" , 0)
    }

    fun playHappyMotion() {
        controller.playMotion("Happy", 0)
    }

    fun playAngryMotion() {
        controller.playMotion("Angry", 0)
    }

    fun playCryingMotion() {
        controller.playMotion("Crying", 0)
    }

    fun playHerMotion() {
        controller.playMotion("Her", 0)
    }

    fun playAppearanceMotion() {
        viewModelScope.launch {
            controller.showCharacter() // 캐릭터 표시
            controller.playMotion("Appearance", 0) // 등장 모션
            controller.appearAtFixedPosition()
        }
    }

    fun playExitMotion() {
        viewModelScope.launch {
            controller.playMotion("Exit", 0) // 퇴장 모션
            controller.disappearAtFixedPosition()
        }
    }
}
