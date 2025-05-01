package com.example.byeoldori.ui.screen.Observatory

import android.location.Geocoder

fun getAddressFromLatLng(  //위도/경도 -> 주소(문자열)
    geocoder: Geocoder,
    lat: Double,
    lng: Double
): String {
    val addresses = geocoder.getFromLocation(lat, lng, 1)
    val address = addresses?.firstOrNull() //리스트에서 첫번째 주소를 가져옴
    val fullAddress = address?.getAddressLine(0) ?: "주소를 찾을 수 없습니다."
    val featureName = address?.featureName ?: ""

    return if (featureName.isNotBlank() && !fullAddress.contains(featureName)) {
        "$fullAddress ($featureName)"
    } else {
        fullAddress
    }
}