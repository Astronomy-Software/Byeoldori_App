package com.example.byeoldori.skymap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.utils.SweObjUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 🛰️ SWE Object 변환 테스트 화면 (뷰모델 통합 버전)
 * - 입력: Stellarium Web Object 형식 or 한국어 이름
 * - 출력: 한글 변환 결과 / SWE 형식 변환 결과
 */
@Composable
fun TestSweScreen(
    modifier: Modifier = Modifier,
    viewModel: TestSweViewModel = viewModel()
) {
    val inputText by viewModel.input.collectAsState()
    val koreanResult by viewModel.korean.collectAsState()
    val sweResult by viewModel.swe.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🛰️ SWE Object 변환 테스트", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = inputText,
            onValueChange = { viewModel.onInputChanged(it) },
            label = { Text("입력 (예: NAME Deneb / 데네브 / Orion Nebula)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.convert() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("변환 실행")
        }

        Divider()

        Text(
            "한글 변환 결과: ${if (koreanResult.isNotBlank()) koreanResult else "—"}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            "SWE 형식 변환 결과: ${if (sweResult.isNotBlank()) sweResult else "—"}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * 🧠 SWE 변환 테스트용 ViewModel
 */
class TestSweViewModel : ViewModel() {

    private val _input = MutableStateFlow("")
    val input = _input.asStateFlow()

    private val _korean = MutableStateFlow("")
    val korean = _korean.asStateFlow()

    private val _swe = MutableStateFlow("")
    val swe = _swe.asStateFlow()

    fun onInputChanged(newValue: String) {
        _input.value = newValue
    }

    fun convert() {
        viewModelScope.launch {
            val name = _input.value.trim()
            _korean.value = SweObjUtils.toKorean(name)
            _swe.value = SweObjUtils.toSweFormat(name)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestSweScreenPreview() {
    TestSweScreen()
}
