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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val uiState by vm.uiState.collectAsState()

    Background(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(top = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Email
            InputForm(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "내용을 입력해 주세요",
                modifier = Modifier.width(330.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 이메일 중복 확인 버튼
            WideButton(
                text = "이메일 중복 확인",
                onClick = { vm.checkEmail(email) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Password
            InputForm(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "내용을 입력해 주세요",
                modifier = Modifier.width(330.dp)
            )

            // Password 확인
            InputForm(
                label = "Password 확인",
                value = passwordConfirm,
                onValueChange = { passwordConfirm = it },
                placeholder = "내용을 입력해 주세요",
                modifier = Modifier.width(330.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 이름
            InputForm(
                label = "이름",
                value = name,
                onValueChange = { name = it },
                placeholder = "이름을 입력해 주세요",
                modifier = Modifier.width(330.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 전화번호
            InputForm(
                label = "전화번호",
                value = phone,
                onValueChange = { phone = it },
                placeholder = "010-1234-5678",
                modifier = Modifier.width(330.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 인증번호 요청 버튼
            WideButton(
                text = "인증번호 받기",
                onClick = { /* TODO: 이메일 인증 API (메일 전송) */ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // 인증번호 입력
            InputForm(
                label = "Email 인증번호",
                value = verificationCode,
                onValueChange = { verificationCode = it },
                placeholder = "내용을 입력해 주세요",
                modifier = Modifier.width(330.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 회원가입 버튼
            WideButton(
                text = "회원가입 하기",
                onClick = {
                    vm.signUp(
                        email = email,
                        password = password,
                        passwordConfirm = passwordConfirm,
                        name = name,
                        phone = phone,
                    )
                    onNext()
                },
                backgroundColor = Color(0xFFBDBDBD),
                contentColor = TextNormal,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(8.dp))

            WideButton(
                text = "뒤로가기",
                onClick = onBack,
                backgroundColor = Color.Gray,
                contentColor = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // 상태 표시
            when (uiState) {
                is SignUpUiState.Loading -> {
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.Text("⏳ 처리 중...")
                }
                is SignUpUiState.Success -> {
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.Text("✅ ${(uiState as SignUpUiState.Success).message}")
                }
                is SignUpUiState.Error -> {
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.Text(
                        "❌ ${(uiState as SignUpUiState.Error).message}",
                        color = Color.Red
                    )
                }
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    Background {
        SignUpScreen(onNext = {}, onBack = {})
    }
}
