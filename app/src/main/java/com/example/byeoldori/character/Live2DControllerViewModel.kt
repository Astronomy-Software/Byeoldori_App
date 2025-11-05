package com.example.byeoldori.character

import androidx.compose.ui.Alignment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

enum class Emotion { Idle, Happy, Angry, Crying, Her }

@Singleton
object Live2DControllerViewModel {

    val controller = Live2DController() // 전역 싱글톤
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun playIdleMotion() = controller.playMotion("Idle", 0)
    fun playHappyMotion() = controller.playMotion("Happy", 0)
    fun playAngryMotion() = controller.playMotion("Angry", 0)
    fun playCryingMotion() = controller.playMotion("Crying", 0)
    fun playHerMotion() = controller.playMotion("Her", 0)

    fun playAppearanceMotion() {
        scope.launch {
            controller.showCharacter()
            controller.playMotion("Appearance", 0)
            controller.appearAtFixedPosition()
        }
    }

    fun playExitMotion() {
        scope.launch {
            controller.playMotion("Exit", 0)
            controller.disappearAtFixedPosition()
        }
    }

    fun chat(
        text: String,
        emotion: Emotion = Emotion.Idle,
        tail: TailPosition = TailPosition.Left,
    ) {
        scope.launch {
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
