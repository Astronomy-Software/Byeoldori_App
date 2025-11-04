package com.example.byeoldori.character

import android.content.res.Resources
import android.view.View
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.live2d.live2dview.LAppLive2DManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow


@Singleton
class Live2DController @Inject constructor() {
    private var live2DView: Live2DGLSurfaceView? = null
    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible

    /** CoroutineScope 추가 */
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /** Live2DView 연결 관리 */
    fun attachView(view: Live2DGLSurfaceView) {
        live2DView = view
        view.visibility = View.GONE   // ✅ 기본값을 숨김으로 설정
    }

    fun detachView() { live2DView = null }

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

    // 🔹 위치/크기 상태
    private val _offsetX = MutableStateFlow(0.dp)
    private val _offsetY = MutableStateFlow(0.dp)
    private val aspectRatio =  9f / 16f
    private val minWidth = 9.dp
    private val minHeight = 16.dp

    private val _width = MutableStateFlow(200.dp)
    private val _height = MutableStateFlow((_width.value.value / aspectRatio).dp)

    val bubbleYOffset: StateFlow<Dp> = _height.map { (it * 0.2f) }.stateIn(scope, SharingStarted.Eagerly, 0.dp)

    // 최종 Modifier 상태
    private val _viewModifier = MutableStateFlow(
        Modifier.size(_width.value, _height.value).offset(_offsetX.value, _offsetY.value)
    )
    val viewModifier: StateFlow<Modifier> = _viewModifier

    /** 내부 Modifier 갱신 */
    private fun refreshModifier() {
        // 항상 비율 3:4 유지
        var newWidth = _width.value
        var newHeight = (newWidth.value / aspectRatio).dp

        // ✅ 최소 크기 강제 적용
        if (newWidth < minWidth) {
            newWidth = minWidth
            newHeight = minHeight
        }
        if (newHeight < minHeight) {
            newHeight = minHeight
            newWidth = minWidth
        }

        _width.value = newWidth
        _height.value = newHeight

        _viewModifier.value = Modifier
            .size(_width.value, _height.value)
            .offset(_offsetX.value, _offsetY.value)
    }

    /** 위치 이동 */
    fun moveBy(dx: Dp, dy: Dp) {
        _offsetX.value += dx
        _offsetY.value += dy
        refreshModifier()
    }
    /** 위치 지정 */
    fun setLocation(dx: Dp, dy: Dp) {
        _offsetX.value = dx
        _offsetY.value = dy
        refreshModifier()
    }
    fun setX(dx: Dp) {
        _offsetX.value = dx
        refreshModifier()
    }
    fun setY(dy: Dp){
        _offsetY.value = dy
        refreshModifier()
    }

    /** 크기 지정 (비율 유지 + 최소 크기 보장) */
    fun setSize(dw: Dp) {
        _width.value = dw
        refreshModifier()
    }

    /** 크기 조정 (비율 유지 + 최소 크기 보장) */
    fun resizeBy(dw: Dp) {
        _width.value += dw
        refreshModifier()
    }

    /** 크기/위치 초기화 */
    fun resetSizeAndPosition() {
        _offsetX.value = 0.dp
        _offsetY.value = 0.dp
        _width.value = 200.dp
        _height.value = (_width.value.value / aspectRatio).dp
        refreshModifier()
    }

    /** Show/Hide */
    fun showCharacter() {
        live2DView?.visibility = View.VISIBLE
        _isVisible.value = true
    }

    fun hideCharacter() {
        live2DView?.visibility = View.GONE
        _isVisible.value = false
    }
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

    /** 모션 목록 새로고침 함수 */
    fun refreshMotions() {
        // UI 스레드에서 OpenGL 명령을 실행할 수 있도록 queueEvent를 사용합니다.
        live2DView?.queueEvent {
            val motionGroups = LAppLive2DManager.getInstance().getAvailableMotionGroups()
            // StateFlow를 갱신합니다.
            scope.launch {
                _motions.value = motionGroups
            }
        }
    }

    /** ✅ X축 중앙 정렬 */
    fun centerHorizontally() {
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidthPx = displayMetrics.widthPixels
        val density = displayMetrics.density
        val screenWidthDp = (screenWidthPx / density).dp

        val characterWidth = _width.value
        val newOffsetX = (screenWidthDp - characterWidth) / 2
        _offsetX.value = newOffsetX
        refreshModifier()
    }

    /** ✅ Y축 중앙 정렬 */
    fun centerVertically() {
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenHeightPx = displayMetrics.heightPixels
        val density = displayMetrics.density
        val screenHeightDp = (screenHeightPx / density).dp

        val characterHeight = _height.value
        val newOffsetY = (screenHeightDp - characterHeight) / 2
        _offsetY.value = newOffsetY
        refreshModifier()
    }

    fun fadeInCharacter(durationMs: Long = 2000L) {
        live2DView?.apply {
            alpha = 0f        // 완전 투명한 상태에서 시작
            visibility = View.VISIBLE
            animate()
                .alpha(1f)    // 1로 천천히 복귀
                .setDuration(durationMs)
                .setStartDelay(0)
                .withStartAction { _isVisible.value = true }
                .withEndAction {
                    alpha = 1f // 혹시 모를 잔여 상태 방지
                }
                .start()
        }
    }

    fun fadeOutCharacter(durationMs: Long = 2000L) {
        live2DView?.apply {
            animate()
                .alpha(0f)    // 서서히 투명해짐
                .setDuration(durationMs)
                .withEndAction {
                    visibility = View.GONE
                    alpha = 1f // 다음 등장 때 다시 보이도록 복구
                    _isVisible.value = false
                }
                .start()
        }
    }

    fun appearAtFixedPosition(
        durationSeconds: Double = 3.0,
        startOffsetXRatio: Float = 1.1f,   // 오른쪽 바깥
        startOffsetYRatio: Float = 0.0f,
        targetXRatio: Float = 0.25f,       // 화면 1/4
        targetYRatio: Float = 0.6f,
        minScale: Float = 0.1f,            // ✅ 아주 작게 시작
        maxScale: Float = 1.0f             // ✅ 최종 크기 제한
    ) {
        scope.launch {
            val metrics = Resources.getSystem().displayMetrics
            val density = metrics.density

            val screenWidthDp: Float
            val screenHeightDp: Float

            if (metrics.widthPixels > metrics.heightPixels) {
                screenWidthDp = metrics.widthPixels / density
                screenHeightDp = metrics.heightPixels / density
            } else {
                screenWidthDp = metrics.heightPixels / density
                screenHeightDp = metrics.widthPixels / density
            }

            // 🎯 캐릭터 크기 보정값 (중심 정렬용)
            val charWidth = _width.value.value
            val charHeight = _height.value.value
            val halfW = charWidth / 2f
            val halfH = charHeight / 2f

            // 📍 시작점 / 도착점 (중심 기준)
            val startX = screenWidthDp * startOffsetXRatio - halfW
            val startY = screenHeightDp * startOffsetYRatio - halfH
            val targetX = screenWidthDp * targetXRatio - halfW
            val targetY = screenHeightDp * targetYRatio - halfH

            val dx = targetX - startX
            val dy = targetY - startY

            live2DView?.apply {
                // 초기 상태 설정
                alpha = 0f
                scaleX = minScale
                scaleY = minScale
                visibility = View.VISIBLE
                _isVisible.value = true

                setLocation(startX.dp, startY.dp)

                val fps = 60
                val frames = (fps * durationSeconds).toInt().coerceAtLeast(1)
                val delayPerFrame = (1000.0 / fps).toLong()

                repeat(frames + 1) { i ->
                    val t = i.toDouble() / frames
                    // 🎢 부드럽게 감속하는 등장 (EaseOutCubic)
                    val eased = 1 - (1 - t).pow(3.0)

                    // 위치 보간
                    val curX = startX + dx * eased
                    val curY = startY + dy * eased
                    setLocation(curX.dp, curY.dp)

                    // ✅ scale 0.1 → 1.0 (maxScale로 제한)
                    val scale = (minScale + (maxScale - minScale) * eased)
                        .coerceIn(minScale.toDouble(), maxScale.toDouble())
                        .toFloat()

                    scaleX = scale
                    scaleY = scale

                    // ✅ 부드러운 fade-in
                    alpha = eased.toFloat().coerceIn(0f, 1f)

                    delay(delayPerFrame)
                }

                alpha = 1f
                scaleX = maxScale
                scaleY = maxScale
                setLocation(targetX.dp, targetY.dp)
            }
        }
    }

    fun disappearAtFixedPosition(
        durationSeconds: Double = 1.8,
        endOffsetXRatio: Float = -0.2f,   // 왼쪽 바깥쪽으로 사라짐
        endOffsetYRatio: Float = 0.0f,
        minScale: Float = 0.1f,           // 사라질 때 최소 크기
        maxScale: Float = 1.0f            // 현재 크기 기준
    ) {
        scope.launch {
            val metrics = Resources.getSystem().displayMetrics
            val density = metrics.density

            val screenWidthDp: Double
            val screenHeightDp: Double

            if (metrics.widthPixels > metrics.heightPixels) {
                screenWidthDp = metrics.widthPixels / density.toDouble()
                screenHeightDp = metrics.heightPixels / density.toDouble()
            } else {
                screenWidthDp = metrics.heightPixels / density.toDouble()
                screenHeightDp = metrics.widthPixels / density.toDouble()
            }

            // 현재 기준 (중심점 계산용)
            val charWidth = _width.value.value.toDouble()
            val charHeight = _height.value.value.toDouble()
            val halfW = charWidth / 2.0
            val halfH = charHeight / 2.0

            // 현재 위치
            val currentX = _offsetX.value.value.toDouble()
            val currentY = _offsetY.value.value.toDouble()

            // 목표 위치 (화면 왼쪽 바깥쪽으로 이동)
            val targetX = screenWidthDp * endOffsetXRatio - halfW
            val targetY = screenHeightDp * endOffsetYRatio - halfH

            val dx = targetX - currentX
            val dy = targetY - currentY

            val baseWidth = _width.value

            live2DView?.apply {
                alpha = 1f
                visibility = View.VISIBLE

                val fps = 60
                val frames = (fps * durationSeconds).toInt().coerceAtLeast(1)
                val delayPerFrame = (1000.0 / fps).toLong()

                var prevX = currentX
                var prevY = currentY

                repeat(frames + 1) { i ->
                    val t = i.toDouble() / frames
                    val eased = t.pow(3.0)

                    // 이동
                    val curX = currentX + dx * eased
                    val curY = currentY + dy * eased
                    val stepX = (curX - prevX).dp
                    val stepY = (curY - prevY).dp
                    moveBy(stepX, stepY)

                    prevX = curX
                    prevY = curY

                    // ✅ 점점 축소
                    val currentScale = (maxScale - (maxScale - minScale) * eased).toDouble()
                        .coerceIn(minScale.toDouble(), maxScale.toDouble())
                    val newWidth = (baseWidth.value.toDouble() * currentScale).dp
                    setSize(newWidth)

                    // ✅ fade-out
                    alpha = (1.0 - eased).toFloat().coerceIn(0f, 1f)

                    delay(delayPerFrame)
                }

                // 사라짐 처리
                visibility = View.GONE
                alpha = 1f
                setSize(baseWidth)
                _isVisible.value = false
            }
        }
    }
}
