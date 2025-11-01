package com.example.byeoldori.skymap

import android.webkit.WebView

/**
 * ✅ Stellarium JS 제어용 Kotlin Controller
 * - WebView에 JS 명령어를 안전하게 전달
 * - window.$stelController 래핑
 */
class StellariumController(private val webView: WebView?) {

    /** JS 명령 실행 래퍼 */
    private fun runJS(jsCommand: String) {
        webView?.evaluateJavascript(
            jsCommand,
            null
        )
    }

    /** 🌌 별자리 토글 */
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

    /** 🧭 방위선 격자 토글 */
    fun toggleAzimuthalGrid(visible: Boolean) {
        runJS("\$stelController.toggleAzimuthalGrid($visible);")
    }

    /** ☁️ 대기/지형 토글 */
    fun toggleAtmosphere(visible: Boolean) {
        runJS("\$stelController.toggleAtmosphere($visible);")
    }

    fun toggleLandscape(visible: Boolean) {
        runJS("\$stelController.toggleLandscape($visible);")
    }

    /** 🪐 DSO 토글 */
    fun toggleDSOs(visible: Boolean) {
        runJS("\$stelController.toggleDSOs($visible);")
    }

    /** 🕓 시간 변경 */
    fun setTime(isoTime: String) {
        // JS에서 new Date(isoTime) 생성 후 전달
        runJS("\$stelController.setTime(new Date('$isoTime'));")
    }

    /** 📍 위치 변경 */
    fun setLocation(lat: Double, lng: Double, alt: Double = 0.0) {
        runJS("\$stelController.setLocation($lat, $lng, $alt);")
    }

    /** 🌓 시야각 변경 (줌 인/아웃) */
    fun setFov(deg: Double) {
        runJS("\$stelController.setFov($deg);")
    }

    /** 🧭 시점 이동 */
    fun setViewDirection(yawDeg: Double, pitchDeg: Double) {
        runJS("\$stelController.setViewDirection($yawDeg, $pitchDeg);")
    }

    fun setEducationMode() { runJS("\$stelController.setEducationMode()") }
}
