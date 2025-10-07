//package com.example.byeoldori.character
//
//import android.view.View
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.size
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//import javax.inject.Singleton
//import kotlin.math.pow
//
//
//@Singleton
//class Live2DController @Inject constructor() {
//    private var live2DView: Live2DView? = null
//
//    /** CoroutineScope 추가 */
//    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//
//    /** Live2DView 연결 관리 */
//    fun attachView(view: Live2DView) { live2DView = view }
//    fun detachView() { live2DView = null }
//
//    /** 말풍선 상태 */
//    private val _speech = MutableStateFlow("오늘은 어떤 별을 관측해볼까?")
//    val speech: StateFlow<String> = _speech
//
//    private val _tailPosition = MutableStateFlow(TailPosition.Center)
//    val tailPosition: StateFlow<TailPosition> = _tailPosition
//
//    private val _alignment = MutableStateFlow(Alignment.TopCenter)
//    val alignment: StateFlow<Alignment> = _alignment
//
//    /** 모션 목록 */
//    private val _motions = MutableStateFlow<List<String>>(emptyList())
//    val motions: StateFlow<List<String>> = _motions
//
//    // 🔹 위치/크기 상태
//    private val _offsetX = MutableStateFlow(0.dp)
//    private val _offsetY = MutableStateFlow(0.dp)
//
//    // ✅ 3:4 비율
//    private val aspectRatio =  9f / 16f
//
//    // ✅ 최소 크기 설정
//    private val minWidth = 180.dp
//    private val minHeight = 320.dp
//
//    private val _width = MutableStateFlow(200.dp)
//    private val _height = MutableStateFlow((_width.value.value / aspectRatio).dp)
//
//    val bubbleYOffset: StateFlow<Dp> = _height.map { (it * 0.1f) }.stateIn(scope, SharingStarted.Eagerly, 0.dp)
//
//    // 최종 Modifier 상태
//    private val _viewModifier = MutableStateFlow(
//        Modifier.size(_width.value, _height.value).offset(_offsetX.value, _offsetY.value)
//    )
//    val viewModifier: StateFlow<Modifier> = _viewModifier
//
//    /** 내부 Modifier 갱신 */
//    private fun refreshModifier() {
//        // 항상 비율 3:4 유지
//        var newWidth = _width.value
//        var newHeight = (newWidth.value / aspectRatio).dp
//
//        // ✅ 최소 크기 강제 적용
//        if (newWidth < minWidth) {
//            newWidth = minWidth
//            newHeight = minHeight
//        }
//        if (newHeight < minHeight) {
//            newHeight = minHeight
//            newWidth = minWidth
//        }
//
//        _width.value = newWidth
//        _height.value = newHeight
//
//        _viewModifier.value = Modifier
//            .size(_width.value, _height.value)
//            .offset(_offsetX.value, _offsetY.value)
//    }
//
//    /** 위치 이동 */
//    fun moveBy(dx: Dp, dy: Dp) {
//        _offsetX.value += dx
//        _offsetY.value += dy
//        refreshModifier()
//    }
//
//    /** 크기 조정 (비율 유지 + 최소 크기 보장) */
//    fun resizeBy(dw: Dp) {
//        _width.value += dw
//        refreshModifier()
//    }
//
//    /** 크기/위치 초기화 */
//    fun resetSizeAndPosition() {
//        _offsetX.value = 0.dp
//        _offsetY.value = 0.dp
//        _width.value = 200.dp
//        _height.value = (_width.value.value / aspectRatio).dp
//        refreshModifier()
//    }
//
//    /** Show/Hide */
//    fun showCharacter() { live2DView?.visibility = View.VISIBLE }
//    fun hideCharacter() { live2DView?.visibility = View.GONE }
//
//    /** 캐릭터 제어 */
//    fun nextCharacter() { live2DView?.nextCharacter() }
//    fun changeCharacter(index: Int) { live2DView?.changeCharacter(index) }
//    fun playMotion(group: String, index: Int) { live2DView?.playMotion(group, index) }
//    fun setExpression(exp: String) { live2DView?.setExpression(exp) }
//
//    /** 말풍선 갱신 */
//    fun showSpeech(text: String, tail: TailPosition, align: Alignment) {
//        _speech.value = text
//        _tailPosition.value = tail
//        _alignment.value = align
//    }
//
//    /** 모션 목록 새로고침 */
//    fun refreshMotions() {
//        _motions.value = live2DView?.getAvailableMotions() ?: emptyList()
//    }
//
//    /** ✅ 애니메이션 이동 (예: 1.5초 동안 X축으로 총 +60dp 이동, 120fps 기준) */
//    fun animateMoveX(durationSeconds: Double, totalDx: Dp) {
//        scope.launch {
//            val fps = 120
//            val frames = (fps * durationSeconds).toInt()  // 총 프레임 수 (소수 → 정수 변환)
//            val step = totalDx / frames
//            val delayPerFrame = (1000.0 / fps).toLong()   // 1프레임당 지연시간 ≈ 8ms
//
//            repeat(frames) {
//                moveBy(step, 0.dp)
//                delay(delayPerFrame)
//            }
//        }
//    }
//
//    /** ✅ Ease-Out 애니메이션 이동 (예: 2초 동안 X축 +60dp 이동, 60fps 기준) */
//    fun animateMoveXEaseOut(durationSeconds: Double, totalDx: Dp) {
//        scope.launch {
//            val fps = 60
//            val frames = (fps * durationSeconds).toInt().coerceAtLeast(1) // 최소 1프레임 이상
//            val delayPerFrame = (1000.0 / fps).toLong()
//
//            repeat(frames) { i ->
//                val t = i.toDouble() / frames   // 0.0 ~ 1.0
//                val easedT = 1 - (1 - t) * (1 - t)  // EaseOutQuad
//
//                val currentX = (totalDx.value * easedT).dp
//                val prevX = if (i == 0) 0.dp
//                else (totalDx.value * (1 - (1 - (i - 1).toDouble() / frames).pow(2.0))).dp
//
//                // 이번 프레임에서 이동할 차이만큼 이동
//                moveBy(currentX - prevX, 0.dp)
//
//                delay(delayPerFrame)
//            }
//        }
//    }
//
//    /** 메모리 정리 */
//    fun clear() {
//        scope.cancel()
//    }
//
//    fun animateCustomSmoothMove(durationSeconds: Double, totalDx: Dp, totalDy: Dp) {
//        scope.launch {
//            val fps = 60
//            val frames = (fps * durationSeconds).toInt().coerceAtLeast(1)
//            val delayPerFrame = (1000.0 / fps).toLong()
//
//            val dxTotal = totalDx.value
//            val dyTotal = totalDy.value
//
//            var prevX = 0f
//            var prevY = 0f
//
//            repeat(frames) { i ->
//                val t = i.toDouble() / frames // 0 ~ 1
//                val easedT = customEase(t)
//
//                val currentX = (dxTotal * easedT).toFloat()
//                val currentY = (dyTotal * easedT).toFloat()
//
//                val stepX = (currentX - prevX).dp
//                val stepY = (currentY - prevY).dp
//                moveBy(stepX, stepY)
//
//                prevX = currentX
//                prevY = currentY
//
//                delay(delayPerFrame)
//            }
//        }
//    }
//
//    // easing 함수
//    private fun customEase(t: Double): Double {
//        return if (t < 0.5) {
//            t * 0.12                // 앞 구간은 거의 직선 (1초 동안 약 12% 진척 → 500dp 중 30dp 정도)
//        } else {
//            0.12 + (1 - (1 - (t - 0.5) * 2).pow(2.0)) * 0.88
//        }
//    }
//}
