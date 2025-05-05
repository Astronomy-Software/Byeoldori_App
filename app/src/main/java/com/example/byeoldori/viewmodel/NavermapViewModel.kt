package com.example.byeoldori.viewmodel

import androidx.lifecycle.ViewModel
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NaverMapViewModel: ViewModel() {

    private val _selectedLatLng = MutableStateFlow<LatLng?>(null)
    val selectedLatLng: StateFlow<LatLng?> = _selectedLatLng.asStateFlow()

    private val _selectedAddress = MutableStateFlow<String?>(null)
    val selectedAddress: StateFlow<String?> = _selectedAddress.asStateFlow()

    fun updateSelectedLatLng(latLng: LatLng) {
        _selectedLatLng.value = latLng
    }

    fun updateSelectedAddress(address: String) {
        _selectedAddress.value = address
    }
}