package com.example.byeoldori.viewmodel.Observatory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.repository.WeatherRepository
import com.example.byeoldori.domain.Observatory.CurrentWeather
import com.example.byeoldori.domain.Observatory.DailyForecast
import com.example.byeoldori.domain.Observatory.HourlyForecast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG_VM = "WeatherVM"

@HiltViewModel
class WeatherViewModel @Inject constructor (
    private val weatherRepository: WeatherRepository
): ViewModel() {

    private val _daily = MutableStateFlow<List<DailyForecast>>(emptyList()) //내부에서 데이터를 바꾸는 가변 상태 흐름
    val daily: StateFlow<List<DailyForecast>> = _daily //외부에서 데이터를 읽는 불변 상태 흐름

    private val _hourly = MutableStateFlow<List<HourlyForecast>>(emptyList())
    val hourly: StateFlow<List<HourlyForecast>> = _hourly

    private val _isLoading = MutableStateFlow(false)//로딩 스피너 표시
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _current = MutableStateFlow<CurrentWeather?>(null)
    val current: StateFlow<CurrentWeather?> = _current


    fun getDaily(lat: Double, lon: Double) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null//이전 에러 초기화
        try {
            Log.d(TAG_VM, "repo.getDaily call: lat=$lat lon=$lon")
            val data = weatherRepository.getDaily(lat, lon)
            Log.d(TAG_VM, "repo.getDaily returned: size=${data.size}")
            _daily.value = data //데이터를 UI상테에 반영
        } catch (e: Exception) {
            Log.e(TAG_VM, "repo.getDaily error: ${e.message}", e)
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    fun getHourly(lat: Double, lon: Double) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            _hourly.value = weatherRepository.getHourly(lat, lon)
        } catch (e: Exception) {
            _error.value = e.message
        } finally { _isLoading.value = false }
    }

    fun getCurrent(lat: Double, lon: Double) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            Log.d(TAG_VM, "getCurrent(lat=$lat, lon=$lon) -> repo.getCurrent")
            val result = weatherRepository.getCurrent(lat, lon)
            Log.d(TAG_VM, "repo.getCurrent returned: $result")
            _current.value = result
        } catch (e: Exception) {
            Log.e(TAG_VM, "getCurrent error: ${e.message}", e)
            _error.value = e.message
            _current.value = null
        } finally {
            _isLoading.value = false
        }
    }
}