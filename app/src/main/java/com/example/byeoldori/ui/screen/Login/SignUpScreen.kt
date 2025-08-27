package com.example.byeoldori.ui.screen.Login

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.ui.components.InputForm
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.TextNormal

@Composable
fun SignUpScreen(
    onRequestVerification: (email: String) -> Unit = {},
    onSignUp: (email: String, password: String, verificationCode: String) -> Unit = { _, _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }


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
                .verticalScroll(rememberScrollState()), // 스크롤 가능
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

            // 인증번호 받기 버튼
            WideButton(
                text = "인증번호 받기",
                onClick = { onRequestVerification(email) },
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
                onClick = { onSignUp(email, password, verificationCode) },
                backgroundColor = Color(0xFFBDBDBD),
                contentColor = TextNormal,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    Background(){
        SignUpScreen()
    }
}
