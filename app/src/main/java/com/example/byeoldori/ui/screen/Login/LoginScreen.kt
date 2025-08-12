package com.example.byeoldori.ui.screen.Login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.ui.components.InputForm
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.TextNormal

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit = { _, _ -> },
    onGoogleLogin: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onFindAccount: () -> Unit = {},
    // ▶ 런타임에서 리소스/Coil 등을 쓰고 싶으면 여기로 전달
    mascotPainter: Painter? = null,
    googlePainter: Painter? = null,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isPreview = LocalInspectionMode.current

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF5C2CA3), Color(0xFF5C2CA3).copy(alpha = 0.88f))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .imePadding(), // 키보드 대응
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, // 세로 가운데
        ) {
            Spacer(Modifier.height(40.dp))

            // ── 로고: Preview-safe 표시 ───────────────────────────────
            PreviewSafeImage(
                painter = if (isPreview) null else mascotPainter,
                size = 160.dp,
                placeholder = "⭐"
            )

            Spacer(Modifier.height(32.dp))

            InputForm(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "Email을 입력해 주세요",
                modifier = Modifier.width(330.dp)
            )

            Spacer(Modifier.height(12.dp))

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

            Spacer(Modifier.height(10.dp))

            // ── 구글 버튼 (아이콘은 없어도 동작) ──────────────────────
            WideButton(
                text = "Google ID로 로그인 하기",
                onClick = onGoogleLogin,
                icon = if (isPreview) null else googlePainter,
                iconDescription = "Google",
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .width(330.dp)
                    .padding(horizontal = 6.dp),
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
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Preview/런타임 모두 안전한 이미지 출력 컴포저블 */
@Composable
private fun PreviewSafeImage(
    painter: Painter?,
    size: Dp,
    placeholder: String = "□"
) {
    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(size)
        )
    } else {
        // Preview 또는 리소스 미제공 시: 이모지/문자로 placeholder
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = placeholder,
                color = Color.White,
                fontSize = 56.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
