package com.example.byeoldori.ui.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.components.InputForm
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.ErrorRed
import com.example.byeoldori.ui.theme.SuccessGreen
import com.example.byeoldori.ui.theme.WarningYellow
import com.example.byeoldori.viewmodel.UiState
import com.example.byeoldori.viewmodel.login.SignUpViewModel

@Composable
fun EmailVerificationScreen(
    onBack: () -> Unit,
    onLogin: () -> Unit,
    vm: SignUpViewModel = hiltViewModel()
) {
    val uiState by vm.verificationState.collectAsState()
    val email = vm.getEmail()

    EmailVerificationContent(
        email = email,
        code = "",
        uiState = uiState,
        onCodeChange = { /* 필요시 VM과 연동 */ },
        onVerify = { vm.verifyEmail(it) },
        onBack = onBack,
        onLogin = onLogin
    )
}

@Composable
fun EmailVerificationContent(
    email: String,
    code: String,
    uiState: UiState<String> = UiState.Idle,
    onCodeChange: (String) -> Unit,
    onVerify: (String) -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit
) {
    val localCode = remember { mutableStateOf(code) }

    Background(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(
                title = "이메일 인증",
                onBack = onBack
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(32.dp)
            ) {
                Text(
                    text = "입력하신 이메일\"${email}\"로 인증코드를 발송했습니다.\n 이메일에서 인증번호를 복사해 넣어주시거나, \n 이메일의 링크를 클릭해주세요.",
                    color = SuccessGreen,
                    modifier = Modifier.padding(bottom = 24.dp),
                    fontSize = 14.sp,
                )
                InputForm(
                    label = "이메일 인증번호",
                    value = localCode.value,
                    onValueChange = {
                        localCode.value = it
                        onCodeChange(it)
                    },
                    placeholder = "이메일로 받은 인증번호 입력",
                    modifier = Modifier.width(330.dp)
                )
                Spacer(Modifier.height(16.dp))
                WideButton(
                    text = "인증하기",
                    onClick = { onVerify(localCode.value) },
                    modifier = Modifier.width(330.dp)
                )

                Spacer(Modifier.height(16.dp))
                when (uiState) {
                    is UiState.Loading -> Text("인증 중...", color = WarningYellow)
                    is UiState.Success -> Text(uiState.data, color = SuccessGreen)
                    is UiState.Error -> Text(uiState.message, color = ErrorRed)
                    else -> {}
                }
                Spacer(Modifier.height(16.dp))
                WideButton(
                    text = "로그인 화면으로",
                    onClick = onLogin,
                    modifier = Modifier.width(330.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmailVerificationScreenPreview() {
    EmailVerificationContent(
        email = "test@example.com",
        code = "",
        uiState = UiState.Idle,
        onCodeChange = {},
        onVerify = {},
        onBack = {},
        onLogin = {}
    )
}
