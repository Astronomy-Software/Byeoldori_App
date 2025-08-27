package com.example.byeoldori.ui.screen.Login

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
import com.example.byeoldori.ui.components.AgreementCheckBox
import com.example.byeoldori.ui.components.MarkdownViewer
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.Purple500

@Composable
fun SignUpConsentScreen(
    onSubmit: (policy: Boolean, profile: Boolean, location: Boolean, marketing: Boolean) -> Unit = { _,_,_,_ -> }
) {
    // 동의 상태
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
                // MarkdownViewer 자체는 wrapContentHeight이므로,
                // 외곽 Box에 높이 제한 + 스크롤을 걸어 상자 안에서만 스크롤되게 함
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

            // 동의 체크들
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
            // 제출 버튼 (필수 2개 체크 시만 활성화)
            WideButton(
                text = "회원 가입",
                onClick = { onSubmit(agreePolicy, agreeProfile, agreeLocation, agreeMarketing) },
                enabled = canSubmit,
                backgroundColor = Purple500,
                modifier = Modifier.width(contentWidth)
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SignUpConsentScreenPreview() {
    // MarkdownViewer는 Preview에서 파일 접근이 어려우므로,
    // 필요하면 MarkdownViewer(lines=샘플)로 오버로드해서 미리보기용으로 바꿔 호출하세요.
    SignUpConsentScreen()
}
