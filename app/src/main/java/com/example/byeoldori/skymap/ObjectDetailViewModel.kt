package com.example.byeoldori.skymap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ObjectDetailViewModel @Inject constructor() : ViewModel() {

    /** 🪐 천체 기본/상세 정보 */
    private val _selectedObject = MutableStateFlow<SkyObjectDetail?>(null)
    val selectedObject: StateFlow<SkyObjectDetail?> = _selectedObject

    /** 🧭 실시간 좌표/밝기 정보 */
    private val _realtimeItems = MutableStateFlow<List<ObjectItem>>(emptyList())
    val realtimeItems: StateFlow<List<ObjectItem>> = _realtimeItems

    /** 👁️ 상세 패널 표시 여부 */
    private val _isDetailVisible = MutableStateFlow(false)
    val isDetailVisible: StateFlow<Boolean> = _isDetailVisible

    /** 상세 정보 업데이트 */
    fun updateSelectedObject(detail: SkyObjectDetail) {
        viewModelScope.launch {
            _selectedObject.emit(detail)
        }
    }

    /** 실시간 항목 업데이트 */
    fun updateRealtimeItems(items: List<ObjectItem>) {
        viewModelScope.launch {
            _realtimeItems.emit(items)
        }
    }

    /** 상세 패널 표시/숨김 제어 */
    fun setDetailVisible(show: Boolean) {
        viewModelScope.launch {
            _isDetailVisible.emit(show)
        }
    }

    /** 선택 해제 시 초기화 */
    fun clearSelection() {
        viewModelScope.launch {
            _selectedObject.emit(null)
            _realtimeItems.emit(emptyList())
            _isDetailVisible.emit(false)
        }
    }
}

/** 🌟 천체 상세 정보 구조체 */
data class SkyObjectDetail(
    val name: String,
    val type: String,
    val wikipediaSummary: String,
    val otherNames: List<String>
)

/** 🧭 실시간 데이터 구조체 */
data class ObjectItem(
    val key: String,
    val value: String
)
