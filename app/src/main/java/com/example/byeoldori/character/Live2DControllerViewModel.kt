package com.example.byeoldori.character

import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// 🎭 감정 Enum 정의
enum class Emotion {
    Idle, Happy, Angry, Crying, Her
}

@HiltViewModel
class Live2DControllerViewModel @Inject constructor(
    val controller: Live2DController
) : ViewModel() {

    // ==============================================================
    // 🎭 감정 모션 전용 메서드
    // ==============================================================
    fun playIdleMotion() = controller.playMotion("Idle", 0)
    fun playHappyMotion() = controller.playMotion("Happy", 0)
    fun playAngryMotion() = controller.playMotion("Angry", 0)
    fun playCryingMotion() = controller.playMotion("Crying", 0)
    fun playHerMotion() = controller.playMotion("Her", 0)

    // ==============================================================
    // 🌟 등장 / 퇴장 (기본 연출)
    // ==============================================================
    fun playAppearanceMotion() {
        viewModelScope.launch {
            controller.showCharacter()
            controller.playMotion("Appearance", 0)
            controller.appearAtFixedPosition()
        }
    }

    fun playExitMotion() {
        viewModelScope.launch {
            controller.playMotion("Exit", 0)
            controller.disappearAtFixedPosition()
        }
    }

    // ==============================================================
    // 💬 통합 chat() — 대사 + 감정모션
    // ==============================================================
    fun chat(
        text: String,
        emotion: Emotion = Emotion.Idle,
        tail: TailPosition = TailPosition.Left,
    ) {
        viewModelScope.launch {
            controller.showCharacter()
            controller.showSpeech(text, tail, Alignment.TopCenter)
            when (emotion) {
                Emotion.Idle -> playIdleMotion()
                Emotion.Happy -> playHappyMotion()
                Emotion.Angry -> playAngryMotion()
                Emotion.Crying -> playCryingMotion()
                Emotion.Her -> playHerMotion()
            }
        }
    }
}
