package com.example.byeoldori.viewmodel.Observatory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.repository.WeatherRepository
import com.example.byeoldori.domain.Observatory.CurrentWeather
import com.example.byeoldori.domain.Observatory.DailyForecast
import com.example.byeoldori.domain.Observatory.HourlyForecast
import com.example.byeoldori.viewmodel.UiState
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

    private val _daily = MutableStateFlow<UiState<List<DailyForecast>>>(UiState.Idle) //내부에서 데이터를 바꾸는 가변 상태 흐름
    val daily: StateFlow<UiState<List<DailyForecast>>> = _daily //외부에서 데이터를 읽는 불변 상태 흐름

    private val _hourly = MutableStateFlow<UiState<List<HourlyForecast>>>(UiState.Idle)
    val hourly: StateFlow<UiState<List<HourlyForecast>>> = _hourly

    private val _current = MutableStateFlow<UiState<CurrentWeather>>(UiState.Idle)
    val current: StateFlow<UiState<CurrentWeather>> = _current


    fun getDaily(lat: Double, lon: Double) = viewModelScope.launch {
        _daily.value = UiState.Loading
        try {
            Log.d(TAG_VM, "repo.getDaily call: lat=$lat lon=$lon")
            val data = weatherRepository.getDaily(lat, lon)
            Log.d(TAG_VM, "repo.getDaily returned: size=${data.size}")
            _daily.value = UiState.Success(data) //데이터를 UI상태에 반영
        } catch (e: Exception) {
            Log.e(TAG_VM, "repo.getDaily error: ${e.message}", e)
            _daily.value = UiState.Error(e.message ?: "중기 데이터를 불러오지 못했습니다.")
        }
    }

    fun getHourly(lat: Double, lon: Double) = viewModelScope.launch {
        _hourly.value = UiState.Loading
        try {
            val data = weatherRepository.getHourly(lat, lon)
            _hourly.value = UiState.Success(data)
        } catch (e: Exception) {
            _hourly.value = UiState.Error(e.message ?: "단기 데이터를 불러오지 못했습니다.")
        }
    }

    fun getCurrent(lat: Double, lon: Double) = viewModelScope.launch {
        _current.value = UiState.Loading
        try {
            Log.d(TAG_VM, "getCurrent(lat=$lat, lon=$lon) -> repo.getCurrent")
            val data = weatherRepository.getCurrent(lat, lon)
            Log.d(TAG_VM, "repo.getCurrent returned: $data")
            if (data == null) {
                _current.value = UiState.Error("현재 날씨 데이터를 불러오지 못했습니다.")
            } else {
                _current.value = UiState.Success(data)
            }
        } catch (e: Exception) {
            Log.e(TAG_VM, "getCurrent error: ${e.message}", e)
            _current.value = UiState.Error(e.message ?: "단기 데이터를 불러오지 못했습니다.")
        }
    }
}