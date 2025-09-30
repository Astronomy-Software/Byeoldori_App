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
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
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
import com.example.byeoldori.ui.theme.TextNormal
import com.example.byeoldori.ui.theme.WarningYellow
import com.example.byeoldori.viewmodel.UiState
import com.example.byeoldori.viewmodel.login.FindEmailViewModel

@Composable
fun FindEmailScreen(
    onBack: () -> Unit,
    vm: FindEmailViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    FindEmailContent(
        onBack = onBack,
        state = state,
        onSubmit = { name, phone -> vm.findEmail(name, phone) }
    )
}

@Composable
fun FindEmailContent(
    onBack: () -> Unit,
    state: UiState<List<String>> = UiState.Idle,
    onSubmit: (String, String) -> Unit = { _, _ -> }
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val isFormValid = name.isNotBlank() && phone.isNotBlank()

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
                title = "이메일 찾기",
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
                    label = "이름",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "이름을 입력하세요",
                    modifier = Modifier.width(330.dp)
                )

                Spacer(Modifier.height(16.dp))

                InputForm(
                    label = "전화번호",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "010-1234-5678",
                    modifier = Modifier.width(330.dp)
                )

                Spacer(Modifier.height(24.dp))

                WideButton(
                    text = "이메일 찾기",
                    onClick = { onSubmit(name, phone) },
                    contentColor = TextNormal,
                    modifier = Modifier.width(330.dp),
                    enabled = isFormValid
                )

                Spacer(Modifier.height(16.dp))

                when (state) {
                    UiState.Idle ->
                        Text("이름과 전화번호를 입력하세요", color = TextNormal)

                    UiState.Loading ->
                        Text("이메일을 찾는 중...", color = WarningYellow)

                    is UiState.Success -> {
                        if (state.data.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "가입된 이메일 목록:",
                                    color = SuccessGreen,
                                    fontSize = 18.sp,
                                    fontWeight = Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                state.data.forEach { email ->
                                    Text(
                                        email,
                                        color = TextNormal,
                                        fontSize = 18.sp,
                                        fontWeight = Bold
                                    )
                                }
                            }
                        } else {
                            Text("이메일을 찾을 수 없습니다.", color = ErrorRed)
                        }
                    }
                    is UiState.Error -> Text(state.message, color = ErrorRed)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun FindEmailScreenPreview() {
    FindEmailContent(
        onBack = {},
        state = UiState.Success(listOf()),
        onSubmit = { _, _ -> }
    )
}
