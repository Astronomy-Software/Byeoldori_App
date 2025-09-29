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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.components.AgreementCheckBox
import com.example.byeoldori.ui.components.MarkdownViewer
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.WideButton
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.viewmodel.login.SignUpViewModel

@Composable
fun SignUpConsentScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    vm: SignUpViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        vm.consentEvent.collect {
            onNext()
        }
    }

    SignUpConsentContent(
        agreePolicy = vm.agreePolicy.collectAsState().value,
        onPolicyChange = { vm.agreePolicy.value = it },
        agreeProfile = vm.agreeProfile.collectAsState().value,
        onProfileChange = { vm.agreeProfile.value = it },
        agreeLocation = vm.agreeLocation.collectAsState().value,
        onLocationChange = { vm.agreeLocation.value = it },
        agreeMarketing = vm.agreeMarketing.collectAsState().value,
        onMarketingChange = { vm.agreeMarketing.value = it },
        onSubmit = { vm.saveConsentsAndProceed() },
        onBack = onBack
    )
}


@Composable
fun SignUpConsentContent(
    agreePolicy: Boolean,
    onPolicyChange: (Boolean) -> Unit,
    agreeProfile: Boolean,
    onProfileChange: (Boolean) -> Unit,
    agreeLocation: Boolean,
    onLocationChange: (Boolean) -> Unit,
    agreeMarketing: Boolean,
    onMarketingChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    val canSubmit = agreePolicy && agreeProfile
    val contentWidth = 330.dp

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
                title = "약관 동의",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MarkdownViewer(
                    assetFileName = "raw/privacy_policy.md",
                    modifier = Modifier.width(contentWidth)
                )

                Spacer(Modifier.height(16.dp))

                AgreementCheckBox(
                    checked = agreePolicy,
                    onCheckedChange = onPolicyChange,
                    text = "개인정보 처리방침 전체 동의 (필수)",
                    modifier = Modifier.width(contentWidth)
                )
                Spacer(Modifier.height(10.dp))

                AgreementCheckBox(
                    checked = agreeProfile,
                    onCheckedChange = onProfileChange,
                    text = "프로필 정보(닉네임·생년월일 등) 제공 동의 (필수)",
                    modifier = Modifier.width(contentWidth)
                )
                Spacer(Modifier.height(10.dp))

                AgreementCheckBox(
                    checked = agreeLocation,
                    onCheckedChange = onLocationChange,
                    text = "위치정보 수집·이용 동의 (선택)",
                    modifier = Modifier.width(contentWidth)
                )
                Spacer(Modifier.height(10.dp))

                AgreementCheckBox(
                    checked = agreeMarketing,
                    onCheckedChange = onMarketingChange,
                    text = "알림(푸시·이메일) 수신 동의 (선택)",
                    modifier = Modifier.width(contentWidth)
                )

                Spacer(Modifier.height(16.dp))

                WideButton(
                    text = "다음으로",
                    onClick = onSubmit,
                    enabled = canSubmit,
                    modifier = Modifier.width(contentWidth)
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}


// 👉 Preview는 Content만 테스트
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SignUpConsentScreenPreview() {
    SignUpConsentContent(
        agreePolicy = true,
        onPolicyChange = {},
        agreeProfile = false,
        onProfileChange = {},
        agreeLocation = false,
        onLocationChange = {},
        agreeMarketing = false,
        onMarketingChange = {},
        onSubmit = {},
        onBack = {}
    )
}