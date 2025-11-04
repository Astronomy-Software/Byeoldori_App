package com.example.byeoldori.skymap

import android.webkit.WebView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * 🌌 Stellarium JS 제어 싱글톤 컨트롤러
 * - WebView 1개 인스턴스를 전역적으로 관리
 * - window.$stelController 호출을 안전하게 래핑
 */
object StellariumController {
    private var webViewRef: WeakReference<WebView>? = null
    private val _isBound = MutableStateFlow(false)
    val isBound = _isBound.asStateFlow()

    fun bindWebView(webView: WebView) {
        webViewRef = WeakReference(webView)
        _isBound.value = true
        println("✅ StellariumController WebView 바인딩 완료")
    }

    fun clearBinding() {
        webViewRef?.clear()
        _isBound.value = false
        println("🧹 StellariumController WebView 연결 해제 완료")
    }

    /** ✅ JS 명령 실행 (모든 함수가 이걸 통해 실행됨) */
    private fun runJS(jsCommand: String) {
        val webView = webViewRef?.get()
        webView?.post {
            webView.evaluateJavascript(jsCommand, null)
        } ?: println("⚠️ StellariumController: WebView가 아직 바인딩되지 않음")
    }

    /** 🌟 별자리선 표시 토글 */
    fun toggleConstellations(visible: Boolean) {
        runJS("\$stelController.toggleConstellations($visible);")
    }

    /** 🕸️ 적도 격자선 (J2000) 토글 */
    fun toggleEquatorialGrid(visible: Boolean) {
        runJS("\$stelController.toggleEquatorialGrid($visible);")
    }

    /** 🕸️ 적도 격자선 (JNow) 토글 */
    fun toggleEquatorialJNowGrid(visible: Boolean) {
        runJS("\$stelController.toggleEquatorialJNowGrid($visible);")
    }

    /** 🧭 방위선 격자선 토글 */
    fun toggleAzimuthalGrid(visible: Boolean) {
        runJS("\$stelController.toggleAzimuthalGrid($visible);")
    }

    /** ☁️ 대기 / 지형 */
    fun toggleAtmosphere(visible: Boolean) {
        runJS("\$stelController.toggleAtmosphere($visible);")
    }

    fun toggleLandscape(visible: Boolean) {
        runJS("\$stelController.toggleLandscape($visible);")
    }

    /** 🪐 심우주 객체 (DSOs) */
    fun toggleDSOs(visible: Boolean) {
        runJS("\$stelController.toggleDSOs($visible);")
    }

    /** 🕓 시간 변경 */
    fun setTime(isoTime: String) {
        runJS("\$stelController.setTime(new Date('$isoTime'));")
    }

    /** 📍 위치 변경 */
    fun setLocation(lat: Double, lng: Double, alt: Double = 0.0) {
        runJS("\$stelController.setLocation($lat, $lng, $alt);")
    }

    /** 🌓 시야각 변경 */
    fun setFov(deg: Double) {
        runJS("\$stelController.setFov($deg);")
    }

    /** 🧭 시점 이동 */
    fun setViewDirection(yawDeg: Double, pitchDeg: Double) {
        runJS("\$stelController.setViewDirection($yawDeg, $pitchDeg);")
    }

    /** 🎓 교육모드 (UI 숨김 등) */
    fun setEducationMode() {
        runJS("\$stelController.setEducationMode();")
    }

    fun setLookUpObject(name : String) {
        runJS("\$stelController.selectAndTrackObjectByName($name)")
    }
}
