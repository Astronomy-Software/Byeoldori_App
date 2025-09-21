package com.example.byeoldori.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.ObservationSite
import com.example.byeoldori.data.model.dto.ObservationSiteRegisterRequest
import com.example.byeoldori.data.repository.ObservationSiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ──────────────────────────────────────────────────────────────
// UI State
// ──────────────────────────────────────────────────────────────
sealed class ObservationUiState {
    object Idle : ObservationUiState()              // 초기/대기
    object Loading : ObservationUiState()           // 로딩 중
    data class Success(val sites: List<ObservationSite>) : ObservationUiState() // 성공
    data class Error(val message: String) : ObservationUiState()                // 에러
}

// ──────────────────────────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────────────────────────
@HiltViewModel
class ObservationSiteViewModel @Inject constructor(
    private val repo: ObservationSiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ObservationUiState>(ObservationUiState.Idle)
    val uiState: StateFlow<ObservationUiState> = _uiState.asStateFlow()

    fun loadAllSites() = viewModelScope.launch {
        _uiState.value = ObservationUiState.Loading
        try {
            val sites = repo.getAllSites()
            _uiState.value = ObservationUiState.Success(sites)
        } catch (e: Exception) {
            _uiState.value = ObservationUiState.Error(e.message ?: "관측지 불러오기 실패")
        }
    }

    fun registerSite(name: String, lat: Double, lon: Double) = viewModelScope.launch {
        _uiState.value = ObservationUiState.Loading
        try {
            repo.registerSite(
                ObservationSiteRegisterRequest(
                    name = name,
                    latitude = lat,
                    longitude = lon
                )
            )
            loadAllSites()
        } catch (e: Exception) {
            _uiState.value = ObservationUiState.Error(e.message ?: "관측지 등록 실패")
        }
    }

    fun deleteSite(name: String) = viewModelScope.launch {
        _uiState.value = ObservationUiState.Loading
        try {
            repo.deleteSite(name)
            loadAllSites()
        } catch (e: Exception) {
            _uiState.value = ObservationUiState.Error(e.message ?: "관측지 삭제 실패")
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Composable UI
// ──────────────────────────────────────────────────────────────
@Composable
fun TestObservationSiteScreen(
    vm: ObservationSiteViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadAllSites()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("관측지 테스트 UI", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(12.dp))

        when (state) {
            is ObservationUiState.Idle -> Text("대기 중…")
            is ObservationUiState.Loading -> CircularProgressIndicator()
            is ObservationUiState.Error -> Text(
                "에러: ${(state as ObservationUiState.Error).message}",
                color = MaterialTheme.colorScheme.error
            )
            is ObservationUiState.Success -> {
                val sites = (state as ObservationUiState.Success).sites
                Column {
                    sites.forEach { site ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${site.name} (${site.latitude}, ${site.longitude})" , color = Color.Red)
                                Button(onClick = { vm.deleteSite(site.name) }) {
                                    Text("삭제")
                                }
                            }
                        }
                    }
                }
            }

            else -> { Text("Error")}
        }

        Spacer(Modifier.height(16.dp))

        var name by remember { mutableStateOf("") }
        var lat by remember { mutableStateOf("") }
        var lon by remember { mutableStateOf("") }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("관측지 이름") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = lat,
            onValueChange = { lat = it },
            label = { Text("위도") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = lon,
            onValueChange = { lon = it },
            label = { Text("경도") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val dLat = lat.toDoubleOrNull() ?: 0.0
                val dLon = lon.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) {
                    vm.registerSite(name, dLat, dLon)
                    name = ""; lat = ""; lon = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("관측지 등록")
        }
    }
}
