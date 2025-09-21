package com.example.byeoldori.data

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.common.TokenData
import com.example.byeoldori.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ──────────────────────────────
// UI 상태 (TestLoginUiState 로 통일)
// ──────────────────────────────
sealed interface TestLoginUiState {
    object Idle : TestLoginUiState
    object Loading : TestLoginUiState
    data class Success(val token: TokenData) : TestLoginUiState
    data class Error(val message: String) : TestLoginUiState
}

// ──────────────────────────────
// ViewModel
// ──────────────────────────────
@HiltViewModel
class TestLoginViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TestLoginUiState>(TestLoginUiState.Idle)
    val uiState: StateFlow<TestLoginUiState> = _uiState

    fun login(email: String, password: String) = viewModelScope.launch {
        _uiState.value = TestLoginUiState.Loading
        try {
            val token = repo.login(email, password)
            _uiState.value = TestLoginUiState.Success(token)
        } catch (e: Exception) {
            _uiState.value = TestLoginUiState.Error(e.message ?: "알 수 없는 오류")
        }
    }

    fun logout() = viewModelScope.launch {
        repo.logout()
        _uiState.value = TestLoginUiState.Idle
    }
}

// ──────────────────────────────
// Compose UI
// ──────────────────────────────
@Composable
fun Test2LoginScreen(
    vm: TestLoginViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔑 로그인", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                vm.login(email.trim(), password)
            }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                vm.login(email.trim(), password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is TestLoginUiState.Loading
        ) {
            Text(if (state is TestLoginUiState.Loading) "로그인 중..." else "로그인")
        }

        Spacer(Modifier.height(12.dp))

        when (state) {
            is TestLoginUiState.Idle -> Text("이메일과 비밀번호를 입력하세요.")
            is TestLoginUiState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
            is TestLoginUiState.Success -> {
                val s = state as TestLoginUiState.Success
                Text("✅ 로그인 성공!")
                Spacer(Modifier.height(4.dp))
                Text("Access: ${s.token.accessToken.take(10)}…")
                Text("Refresh: ${s.token.refreshToken.take(10)}…")
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("로그아웃", color = MaterialTheme.colorScheme.onError)
                }
            }
            is TestLoginUiState.Error -> {
                val e = state as TestLoginUiState.Error
                Text("❌ 오류: ${e.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
