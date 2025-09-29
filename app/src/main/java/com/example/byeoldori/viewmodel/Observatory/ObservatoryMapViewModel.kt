package com.example.byeoldori.viewmodel.Observatory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.ObservationSite
import com.example.byeoldori.data.repository.ObservationSiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ObservatoryMapViewModel @Inject constructor(
    private val observatoryRepository: ObservationSiteRepository
) : ViewModel() {

    var sites by mutableStateOf<List<ObservationSite>>(emptyList())

    var isLoading by mutableStateOf(false)

    var error by mutableStateOf<String?>(null)

    init {
        loadSites()
    }

    fun loadSites() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                sites = observatoryRepository.getAllSites()
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
}