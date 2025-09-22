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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.components.InputForm
import com.example.byeoldori.ui.components.SecretInputForm
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.TextNormal
import com.example.byeoldori.viewmodel.SignUpUiState
import com.example.byeoldori.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    vm: SignUpViewModel = hiltViewModel()
) {
    val uiState = vm.uiState.collectAsState().value

    // ✅ 회원가입 성공 시 다음 화면 이동
    LaunchedEffect(uiState) {
        if (uiState is SignUpUiState.Success) {
            onNext()
        }
    }

    SignUpScreenContent(
        email = vm.email.value,
        onEmailChange = { vm.email.value = it },
        password = vm.password.value,
        onPasswordChange = { vm.password.value = it },
        passwordConfirm = vm.passwordConfirm.value,
        onPasswordConfirmChange = { vm.passwordConfirm.value = it },
        name = vm.name.value,
        onNameChange = { vm.name.value = it },
        phone = vm.phone.value,
        onPhoneChange = { vm.phone.value = it },
        uiState = uiState,
        onSignUp = { vm.signUp() },
        onBack = onBack
    )
}

// 👉 UI 전용 Content
@Composable
fun SignUpScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordConfirm: String,
    onPasswordConfirmChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    uiState: SignUpUiState = SignUpUiState.Idle,
    onSignUp: () -> Unit,
    onBack: () -> Unit,
) {
    val isFormValid = email.isNotBlank() &&
            password.isNotBlank() &&
            passwordConfirm.isNotBlank() &&
            name.isNotBlank() &&
            phone.isNotBlank()

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
            // ✅ TopBar
            TopBar(
                title = "회원가입",
                onBack = onBack
            )

            // ✅ 입력 폼 (스크롤 가능)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                InputForm(
                    label = "Email",
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = "내용을 입력해 주세요",
                    modifier = Modifier.width(330.dp)
                )

                SecretInputForm(
                    label = "Password",
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = "내용을 입력해 주세요",
                    modifier = Modifier.width(330.dp)
                )

                SecretInputForm(
                    label = "Password 확인",
                    value = passwordConfirm,
                    onValueChange = onPasswordConfirmChange,
                    placeholder = "내용을 입력해 주세요",
                    modifier = Modifier.width(330.dp)
                )

                InputForm(
                    label = "이름",
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = "이름을 입력해 주세요",
                    modifier = Modifier.width(330.dp)
                )

                InputForm(
                    label = "전화번호",
                    value = phone,
                    onValueChange = onPhoneChange,
                    placeholder = "010-1234-5678",
                    modifier = Modifier.width(330.dp)
                )

                Spacer(Modifier.height(16.dp))

                WideButton(
                    text = "회원가입 하기",
                    onClick = onSignUp,
                    contentColor = TextNormal,
                    modifier = Modifier.width(330.dp),
                    enabled = isFormValid
                )

                // ✅ 상태 표시
                when (uiState) {
                    is SignUpUiState.Loading ->
                        androidx.compose.material3.Text("⏳ 처리 중...")
                    is SignUpUiState.Success ->
                        androidx.compose.material3.Text("✅ ${uiState.message}")
                    is SignUpUiState.Error ->
                        androidx.compose.material3.Text(
                            "❌ ${uiState.message}",
                            color = Color.Red
                        )
                    else -> {}
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// 👉 Preview는 Content만 테스트
@Preview(showBackground = true)
@Composable
fun SignUpScreenContentPreview() {
    SignUpScreenContent(
        email = "",
        onEmailChange = {},
        password = "",
        onPasswordChange = {},
        passwordConfirm = "",
        onPasswordConfirmChange = {},
        name = "",
        onNameChange = {},
        phone = "",
        onPhoneChange = {},
        uiState = SignUpUiState.Idle,
        onSignUp = {},
        onBack = {}
    )
}
