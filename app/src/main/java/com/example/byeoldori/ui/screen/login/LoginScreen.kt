package com.example.byeoldori.ui.screen.login

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.character.Live2DControllerViewModel
import com.example.byeoldori.character.TailPosition
import com.example.byeoldori.ui.components.InputForm
import com.example.byeoldori.ui.components.SecretInputForm
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.ErrorRed
import com.example.byeoldori.ui.theme.SuccessGreen
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.ui.theme.TextNormal
import com.example.byeoldori.ui.theme.WarningYellow
import com.example.byeoldori.viewmodel.AuthViewModel
import com.example.byeoldori.viewmodel.UiState

@Composable
fun LoginScreen(
    onSignUp: () -> Unit,
    onFindEmail: () -> Unit,
    onResetPassword: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val uiState by vm.state.collectAsState()

    LoginContent(
        uiState = uiState,
        onLogin = { email, password -> vm.login(email, password) },
        onSignUp = onSignUp,
        onFindEmail = onFindEmail,
        onResetPassword = onResetPassword,
    )
}

@Composable
private fun LoginContent(
    uiState: UiState<Any?> = UiState.Idle,
    onLogin: (String, String) -> Unit,
    onSignUp: () -> Unit,
    onFindEmail: () -> Unit,
    onResetPassword : () -> Unit,
    mascotVM: Live2DControllerViewModel = hiltViewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val mascotController = mascotVM.controller

    LaunchedEffect(Unit) {
        mascotController.showCharacter()
        mascotController.setSize(250.dp)
        mascotController.setLocation(0.dp,10.dp)
        mascotController.centerHorizontally()
        mascotController.showSpeech("로그인이나 회원가입을 하고\n같이 별보러가자!", TailPosition.Center, Alignment.TopCenter)
    }

    // 🧹 화면 떠날 때 마스코트 퇴장
    DisposableEffect(Unit) {
        onDispose {
            mascotController.hideCharacter()
        }
    }

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
            Spacer( // 별도리 있을 공간
                modifier = Modifier
                    .width(330.dp)
                    .height(330.dp)
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
            SecretInputForm(
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
                onClick = { onLogin(email, password) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            // Google 로그인 버튼
            WideButton(
                text = "Google ID로 로그인 하기",
                onClick = {
                    /* TODO: Google OAuth 연동 */
                    Toast.makeText(context, "아직 미완성된 기능입니다.", Toast.LENGTH_SHORT).show()
                },
                icon = R.drawable.ic_google_logo,
                iconDescription = "Google",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(330.dp)
            )

            Spacer(Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier.width(330.dp),
                thickness = 3.dp,
                color = TextHighlight.copy(alpha = 0.5f)
            )

            Spacer(Modifier.height(8.dp))

            // 회원가입 / 계정찾기
            Row(
                modifier = Modifier.width(330.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 회원가입 (왼쪽)
                Text(
                    text = "회원가입",
                    color = TextNormal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSignUp() }
                )

                // 오른쪽 묶음 (이메일 찾기 | 비밀번호 찾기)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Email 찾기",
                        color = TextNormal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onFindEmail() }
                    )

                    // 작은 구분선 (세로선처럼)
                    VerticalDivider(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .height(14.dp)
                            .width(1.dp),
                        thickness = 2.dp,
                        color = TextHighlight
                    )

                    Text(
                        text = "비밀번호 찾기",
                        color = TextNormal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onResetPassword() }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when (uiState) {
                is UiState.Loading -> Text("로그인 중...", color = WarningYellow)
                is UiState.Success -> Text("로그인 성공!", color = SuccessGreen)
                is UiState.Error -> Text(uiState.message, color = ErrorRed)
                else -> {}
            }
        }
    }
}

// ✅ Preview는 Content만
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun LoginScreenPreview() {
    LoginContent(
        uiState = UiState.Idle,
        onLogin = { _, _ -> },
        onSignUp = {},
        onFindEmail = {},
        onResetPassword = {},
    )
}
