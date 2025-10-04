package com.example.byeoldori.character

import android.view.View
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.live2d.live2dview.Live2DView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Live2DController @Inject constructor() {
    private var live2DView: Live2DView? = null

    /** Live2DView 연결 관리 */
    fun attachView(view: Live2DView) {
        live2DView = view
    }
    fun detachView() {
        live2DView = null
    }

    /** 말풍선 상태 */
    private val _speech = MutableStateFlow("오늘은 어떤 별을 관측해볼까?")
    val speech: StateFlow<String> = _speech

    private val _tailPosition = MutableStateFlow(TailPosition.Center)
    val tailPosition: StateFlow<TailPosition> = _tailPosition

    private val _alignment = MutableStateFlow(Alignment.TopCenter)
    val alignment: StateFlow<Alignment> = _alignment

    /** 모션 목록 */
    private val _motions = MutableStateFlow<List<String>>(emptyList())
    val motions: StateFlow<List<String>> = _motions

    /** Modifier 상태 → 외부에서 크기/위치 제어 가능 */
    private val _viewModifier = MutableStateFlow(Modifier.size(200.dp, 300.dp))
    val viewModifier: StateFlow<Modifier> = _viewModifier
    fun updateModifier(modifier: Modifier) { _viewModifier.value = modifier }

    /** Show/Hide */
    fun showCharacter() { live2DView?.visibility = View.VISIBLE }
    fun hideCharacter() { live2DView?.visibility = View.GONE }

    /** 캐릭터 제어 */
    fun nextCharacter() { live2DView?.nextCharacter() }
    fun changeCharacter(index: Int) { live2DView?.changeCharacter(index) }
    fun playMotion(group: String, index: Int) { live2DView?.playMotion(group, index) }
    fun setExpression(exp: String) { live2DView?.setExpression(exp) }

    /** 말풍선 갱신 */
    fun showSpeech(text: String, tail: TailPosition, align: Alignment) {
        _speech.value = text
        _tailPosition.value = tail
        _alignment.value = align
    }

    /** 모션 목록 새로고침 */
    fun refreshMotions() {
        _motions.value = live2DView?.getAvailableMotions() ?: emptyList()
    }
}
