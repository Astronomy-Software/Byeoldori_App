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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.byeoldori.ui.components.SecretInputForm
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.ErrorRed
import com.example.byeoldori.ui.theme.TextNormal
import com.example.byeoldori.viewmodel.login.SignUpUiState
import com.example.byeoldori.viewmodel.login.SignUpViewModel

@Composable
fun SignUpScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    vm: SignUpViewModel = hiltViewModel()
) {
    val uiState = vm.uiState.collectAsState().value

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // ✅ 이벤트 수집
    LaunchedEffect(Unit) {
        vm.signUpEvent.collect { message ->
            successMessage = message
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("회원가입 완료") },
            text = { Text(successMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onNext()
                }) {
                    Text("확인")
                }
            }
        )
    }

    SignUpScreenContent(
        email = vm.email.collectAsState().value,
        onEmailChange = { vm.email.value = it },
        password = vm.password.collectAsState().value,
        onPasswordChange = { vm.password.value = it },
        passwordConfirm = vm.passwordConfirm.collectAsState().value,
        onPasswordConfirmChange = { vm.passwordConfirm.value = it },
        name = vm.name.collectAsState().value,
        onNameChange = { vm.name.value = it },
        phone = vm.phone.collectAsState().value,
        onPhoneChange = { vm.phone.value = it },
        uiState = uiState,
        onSignUp = { vm.signUp() },
        onBack = onBack
    )
}

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
            TopBar(title = "회원가입", onBack = onBack)

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

                Spacer(Modifier.height(12.dp))

                when (uiState) {
                    is SignUpUiState.Loading ->
                        Text("회원가입 처리 중...")
                    is SignUpUiState.Error ->
                        Text(uiState.message, color = ErrorRed)
                    else -> {}
                }
            }
        }
    }
}

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
