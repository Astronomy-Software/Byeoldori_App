package com.example.byeoldori.viewmodel

import androidx.lifecycle.ViewModel
import com.example.byeoldori.data.repository.NavermapRepository
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class NaverMapViewModel @Inject constructor(
    private val repo: NavermapRepository
) : ViewModel() {

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

    suspend fun reverseAddressRoad(lat: Double, lon: Double): String {
        return repo.reverseAddressRoad(lat, lon)
    }
}