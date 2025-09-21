
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
    val uiState by vm.uiState.collectAsState()

    SignUpScreenContent(
        onNext = onNext,
        onBack = onBack,
        uiState = uiState,
        onCheckEmail = vm::checkEmail,
        onSignUp = vm::signUp
    )
}

@Composable
fun SignUpScreenContent(
    onNext: () -> Unit,
    onBack: () -> Unit,
    uiState: SignUpUiState = SignUpUiState.Idle,
    onCheckEmail: (String) -> Unit = {},
    onSignUp: (String, String, String, String, String) -> Unit = { _,_,_,_,_ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

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
            // ✅ TopBar 고정
            TopBar(
                title = "회원정보 입력",
                onBack = onBack
            )

            // ✅ 입력 폼은 스크롤 가능
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
                    onValueChange = { email = it },
                    placeholder = "내용을 입력해 주세요",
                    modifier = Modifier.width(330.dp)
                )

                SecretInputForm(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "내용을 입력해 주세요",
                    modifier = Modifier.width(330.dp)
                )

                SecretInputForm(
                    label = "Password 확인",
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    placeholder = "내용을 입력해 주세요",
                    modifier = Modifier.width(330.dp)
                )

                InputForm(
                    label = "이름",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "이름을 입력해 주세요",
                    modifier = Modifier.width(330.dp)
                )

                InputForm(
                    label = "전화번호",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "010-1234-5678",
                    modifier = Modifier.width(330.dp)
                )

                Spacer(Modifier.height(16.dp))

                WideButton(
                    text = "인증번호 받기",
                    onClick = { /* TODO */ },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                InputForm(
                    label = "Email 인증번호",
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    placeholder = "내용을 입력해 주세요",
                    modifier = Modifier.width(330.dp)
                )

                Spacer(Modifier.height(16.dp))

                WideButton(
                    text = "회원가입 하기",
                    onClick = {
                        onSignUp(email, password, passwordConfirm, name, phone)
                        onNext()
                    },
                    backgroundColor = Color(0xFFBDBDBD),
                    contentColor = TextNormal,
                    modifier = Modifier.width(330.dp)
                )

                // 상태 표시
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

@Preview(showBackground = true)
@Composable
fun SignUpScreenContentPreview() {
    SignUpScreenContent(
        onNext = {},
        onBack = {},
        uiState = SignUpUiState.Idle
    )
}
