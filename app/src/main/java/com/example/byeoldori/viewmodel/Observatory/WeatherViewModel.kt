package com.example.byeoldori.viewmodel.Observatory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    // 임시 위치: 우암산 전망대
    private val testLat = 36.65003430206848
    private val testLon = 127.50494706148991

    fun getDaily() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null//이전 에러 초기화
        try {
            Log.d("WeatherVM", "repo.getDaily call: lat=$testLat lon=$testLon")
            val data = weatherRepository.getDaily(testLat, testLon)
            Log.d("WeatherVM", "repo.getDaily returned: size=${data.size}")
            _daily.value = data //데이터를 UI상테에 반영
        } catch (e: Exception) {
            Log.e("WeatherVM", "repo.getDaily error: ${e.message}", e)
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    fun getHourly() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            _hourly.value = weatherRepository.getHourly(testLat, testLon)
        } catch (e: Exception) {
            _error.value = e.message
        } finally { _isLoading.value = false }
    }
}