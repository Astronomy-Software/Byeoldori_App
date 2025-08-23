package com.example.byeoldori.ui.screen.Login

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.InputForm
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.TextNormal

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit = { _, _ -> },
    onGoogleLogin: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onFindAccount: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Background(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(), // 키보드 대응
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, // 세로 가운데
        ) {
            Image(
                painter = painterResource(R.drawable.byeoldori),
                contentDescription = "앱 로고",
                modifier = Modifier.width(300.dp).height(300.dp)
            )
            InputForm(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "Email을 입력해 주세요",
                modifier = Modifier.width(330.dp)
            )
            InputForm(
                label = "PassWord",
                value = password,
                onValueChange = { password = it },
                placeholder = "비밀번호를 입력해 주세요",
                modifier = Modifier.width(330.dp)
            )
            Spacer(Modifier.height(16.dp))
            WideButton(
                text = "일반 로그인",
                onClick = { onLogin(email, password) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))
            WideButton(
                text = "Google ID로 로그인 하기",
                onClick = onGoogleLogin,
                icon = R.drawable.ic_google_logo,
                iconDescription = "Google",
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .width(330.dp),
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
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
