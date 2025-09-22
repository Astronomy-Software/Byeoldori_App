// ui/screen/login/ResetPasswordScreen.kt
package com.example.byeoldori.ui.screen.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.components.InputForm
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.TextNormal
import com.example.byeoldori.viewmodel.Login.ResetPasswordUiState
import com.example.byeoldori.viewmodel.Login.ResetPasswordViewModel

// ✅ 실제 실행 시 VM 연결
@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    vm: ResetPasswordViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    ResetPasswordContent(
        onBack = onBack,
        state = state,
        onSubmit = { email -> vm.resetPassword(email) }
    )
}

// ✅ 순수 UI
@Composable
fun ResetPasswordContent(
    onBack: () -> Unit,
    state: ResetPasswordUiState = ResetPasswordUiState.Idle,
    onSubmit: (String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    val isFormValid = email.isNotBlank()

    Background(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(
                title = "비밀번호 재설정",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InputForm(
                    label = "가입한 이메일",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "example@email.com",
                    modifier = Modifier.width(330.dp)
                )

                Spacer(Modifier.height(24.dp))

                WideButton(
                    text = "재설정 메일 보내기",
                    onClick = { onSubmit(email) },
                    contentColor = TextNormal,
                    modifier = Modifier.width(330.dp),
                    enabled = isFormValid
                )

                Spacer(Modifier.height(16.dp))

                when (state) {
                    is ResetPasswordUiState.Idle ->
                        Text("📩 가입한 이메일을 입력하세요", color = Color.Gray)

                    is ResetPasswordUiState.Loading ->
                        Text("⏳ 요청 중...", color = Color.Gray)

                    is ResetPasswordUiState.Success ->
                        Text("✅ ${(state as ResetPasswordUiState.Success).message}", color = Color.Green)

                    is ResetPasswordUiState.Error ->
                        Text("❌ ${(state as ResetPasswordUiState.Error).message}", color = Color.Red)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ResetPasswordPreview() {
    ResetPasswordContent(
        onBack = {},
        state = ResetPasswordUiState.Success("테스트: 재설정 메일 발송 완료")
    )
}
