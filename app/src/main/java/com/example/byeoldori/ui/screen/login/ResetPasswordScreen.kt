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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.components.InputForm
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.ErrorRed
import com.example.byeoldori.ui.theme.SuccessGreen
import com.example.byeoldori.ui.theme.TextNormal
import com.example.byeoldori.ui.theme.WarningYellow
import com.example.byeoldori.viewmodel.UiState
import com.example.byeoldori.viewmodel.login.ResetPasswordViewModel

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    vm: ResetPasswordViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    ResetPasswordContent(
        onBack = onBack,
        state = state,
        onSubmit = { email,name,phone -> vm.resetPassword(email, name, phone) }
    )
}

@Composable
fun ResetPasswordContent(
    onBack: () -> Unit,
    state: UiState<String> = UiState.Idle,
    onSubmit: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    val isFormValid = email.isNotBlank() && phone.isNotBlank() && name.isNotBlank()

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

                InputForm(
                    label = "전화번호",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "01012345678",
                    modifier = Modifier.width(330.dp)
                )
                InputForm(
                    label = "이름",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "홍길동",
                    modifier = Modifier.width(330.dp)
                )
                Spacer(Modifier.height(24.dp))
                WideButton(
                    text = "재설정 메일 보내기",
                    onClick = { onSubmit(email,name,phone) },
                    contentColor = TextNormal,
                    modifier = Modifier.width(330.dp),
                    enabled = isFormValid
                )

                Spacer(Modifier.height(16.dp))

                when (state) {
                    UiState.Idle ->
                        Text("이메일, 전화번호, 이름을 입력하세요", color = TextNormal)

                    UiState.Loading ->
                        Text("요청 중...", color = WarningYellow)

                    is UiState.Success ->
                        Text(state.data, color = SuccessGreen)

                    is UiState.Error ->
                        Text(state.message, color = ErrorRed)
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
        state = UiState.Success("테스트: 재설정 메일 발송 완료")
    )
}
