// ui/screen/login/SignUpConsentScreen.kt
package com.example.byeoldori.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.components.AgreementCheckBox
import com.example.byeoldori.ui.components.MarkdownViewer
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.viewmodel.SignUpViewModel

@Composable
fun SignUpConsentScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    vm: SignUpViewModel = hiltViewModel()
) {
    var agreePolicy by remember { mutableStateOf(false) }     // (필수)
    var agreeProfile by remember { mutableStateOf(false) }    // (필수)
    var agreeLocation by remember { mutableStateOf(false) }   // (선택)
    var agreeMarketing by remember { mutableStateOf(false) }  // (선택)

    val canSubmit = agreePolicy && agreeProfile
    val contentWidth = 330.dp

    Background(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 정책 본문 카드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    MarkdownViewer(
                        assetFileName = "privacy_policy.md",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 체크박스들
            AgreementCheckBox(
                checked = agreePolicy,
                onCheckedChange = { agreePolicy = it },
                text = "개인정보 처리방침 전체 동의 (필수)",
                modifier = Modifier.width(contentWidth)
            )
            Spacer(Modifier.height(10.dp))

            AgreementCheckBox(
                checked = agreeProfile,
                onCheckedChange = { agreeProfile = it },
                text = "프로필 정보(닉네임·생년월일 등) 제공 동의 (필수)",
                modifier = Modifier.width(contentWidth)
            )
            Spacer(Modifier.height(10.dp))

            AgreementCheckBox(
                checked = agreeLocation,
                onCheckedChange = { agreeLocation = it },
                text = "위치정보 수집·이용 동의 (선택)",
                modifier = Modifier.width(contentWidth)
            )
            Spacer(Modifier.height(10.dp))

            AgreementCheckBox(
                checked = agreeMarketing,
                onCheckedChange = { agreeMarketing = it },
                text = "알림(푸시·이메일) 수신 동의 (선택)",
                modifier = Modifier.width(contentWidth)
            )

            Spacer(Modifier.height(16.dp))

            // 제출 버튼
            WideButton(
                text = "회원 가입",
                onClick = {
                    vm.saveConsents(
                        policy = agreePolicy,
                        profile = agreeProfile,
                        location = agreeLocation,
                        marketing = agreeMarketing
                    )
                    onNext()
                },
                enabled = canSubmit,
                backgroundColor = Purple500,
                modifier = Modifier.width(contentWidth)
            )

            Spacer(Modifier.height(8.dp))

            // 뒤로가기 버튼
            WideButton(
                text = "뒤로가기",
                onClick = onBack,
                backgroundColor = Color.Gray,
                modifier = Modifier.width(contentWidth)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SignUpConsentScreenPreview() {
    SignUpConsentScreen(
        onNext = {},
        onBack = {}
    )
}
