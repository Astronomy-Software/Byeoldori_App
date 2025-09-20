package com.example.byeoldori.ui.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.InputForm
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.TextNormal
import com.example.byeoldori.viewmodel.AuthViewModel
import com.example.byeoldori.viewmodel.UiState

@Composable
fun LoginScreen(
    onSignUp: () -> Unit,
    onFindAccount: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by vm.state.collectAsState()

    Background(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.byeoldori),
                contentDescription = "앱 로고",
                modifier = Modifier.width(300.dp).height(300.dp)
            )

            // 이메일 입력
            InputForm(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "Email을 입력해 주세요",
                modifier = Modifier.width(330.dp)
            )

            // 비밀번호 입력
            InputForm(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "비밀번호를 입력해 주세요",
                modifier = Modifier.width(330.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 일반 로그인 버튼
            WideButton(
                text = "일반 로그인",
                onClick = { vm.login(email, password) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            // Google 로그인 버튼 (TODO: 실제 구현 필요)
            WideButton(
                text = "Google ID로 로그인 하기",
                onClick = { /* TODO: Google OAuth 연동 */ },
                icon = R.drawable.ic_google_logo,
                iconDescription = "Google",
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 회원가입 / 계정찾기
            Row(
                modifier = Modifier.width(330.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "회원가입",
                    color = TextNormal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSignUp() }
                )
                Text(
                    text = "ID/비밀번호 찾기",
                    color = TextNormal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onFindAccount() }
                )
            }

            Spacer(Modifier.height(16.dp))

            // 로그인 상태 메시지 표시
            when (uiState) {
                is UiState.Loading -> Text("⏳ 로그인 중...")
                is UiState.Success -> Text("✅ 로그인 성공!", color = Color.Green)
                is UiState.Error -> Text(
                    "❌ ${(uiState as UiState.Error).message}",
                    color = Color.Red
                )
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onSignUp = {},
        onFindAccount = {}
    )
}
