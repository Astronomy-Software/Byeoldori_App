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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.components.InputForm
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.ErrorRed
import com.example.byeoldori.ui.theme.SuccessGreen
import com.example.byeoldori.viewmodel.SignUpViewModel
import com.example.byeoldori.viewmodel.VerificationUiState

// ✅ Wrapper (실제 실행 시 VM 연결)
@Composable
fun EmailVerificationScreen(
    onBack: () -> Unit,
    onLogin: () -> Unit,
    vm: SignUpViewModel = hiltViewModel()
) {
    val uiState by vm.verificationState.collectAsState()
    val email = vm.getEmail() ?: ""   // ✅ VM에서 이메일 가져오기

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

// ✅ UI 전용 Content
@Composable
fun EmailVerificationContent(
    email: String,
    code: String,
    uiState: VerificationUiState = VerificationUiState.Idle,
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
                    text = "입력하신 이메일\n${email}\n로 인증코드를 발송했습니다.",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
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
                    is VerificationUiState.Loading -> Text("인증 중...")
                    is VerificationUiState.Success -> Text("인증 성공 ${uiState.message}", color = SuccessGreen)
                    is VerificationUiState.Error -> Text("인증 실패 ${uiState.message}", color = ErrorRed)
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun EmailVerificationScreenPreview() {
    EmailVerificationContent(
        email = "test@example.com",
        code = "",
        uiState = VerificationUiState.Idle,
        onCodeChange = {},
        onVerify = {},
        onBack = {},
        onLogin = {}
    )
}
