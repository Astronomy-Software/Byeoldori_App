package com.example.byeoldori.viewmodel.Observatory

import android.content.Context
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.GroundOverlay
import com.naver.maps.map.overlay.OverlayImage
import com.example.byeoldori.R
import com.naver.maps.geometry.LatLngBounds

fun addLightPollutionOverlay(context: Context, naverMap: NaverMap): GroundOverlay {
    val overlayImage = OverlayImage.fromResource(R.drawable.korea)

    val bounds = LatLngBounds(
        LatLng(32.0, 123.5), // 남서쪽
        LatLng(40.5, 132.5)  // 북동쪽
    )

    val overlay = GroundOverlay()
    overlay.image = overlayImage
    overlay.setBounds(bounds)
    overlay.alpha = 0.5f //(0.0이면 완전 투명)
    overlay.map = naverMap

    return overlay
}