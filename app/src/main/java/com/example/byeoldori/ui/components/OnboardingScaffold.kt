package com.example.byeoldori.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.R

@Composable
fun OnboardingScaffold(
    contentWidth: Dp = 330.dp,
    illustration: Int,                        // R.drawable.별캐릭터
    body: @Composable ColumnScope.() -> Unit, // 본문 슬롯 (입력/체크 등)
    bottom: @Composable ColumnScope.() -> Unit// 하단 슬롯 (버튼/안내)
) {
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF5C2CA3), Color(0xE65C2CA3))
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 일러스트
            Image(
                painter = painterResource(illustration),
                contentDescription = null,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp).width(220.dp)
            )

            // 본문
            Column(
                modifier = Modifier.width(contentWidth),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) { body() }

            Spacer(Modifier.height(16.dp))

            // 하단
            Column(
                modifier = Modifier.width(contentWidth),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { bottom() }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun ProfileStep(
    onNext: (nickname: String, birth6: String) -> Unit = { _, _ -> }
) {
    var nickname by rememberSaveable { mutableStateOf("") }
    var birth by rememberSaveable { mutableStateOf("") } // YYMMDD

    val nicknameOk = nickname.trim().length in 2..12
    val birthOk = birth.matches(Regex("""\d{6}"""))
    val canNext = nicknameOk && birthOk

    OnboardingScaffold(
        illustration = R.drawable.byeoldori,
        body = {
            Text("앱을 실행하기에 앞서 닉네임과 생년월일을 알려줘!!",
                color = Color.White, fontSize = 16.sp)
            InputForm(
                label = "닉네임",
                value = nickname,
                onValueChange = { nickname = it.take(20) },
                placeholder = "닉네임을 입력해 주세요",
                modifier = Modifier.fillMaxWidth()
            )
            InputForm(
                label = "생년월일",
                value = birth,
                onValueChange = { s -> birth = s.filter(Char::isDigit).take(6) },
                placeholder = "YYMMDD 예) 000710",
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottom = {
            WideButton(
                text = "다음으로",
                onClick = { onNext(nickname.trim(), birth) },
                enabled = canNext
            )
        }
    )
}

@Composable
fun ExperienceStep(
    onDone: (levels: List<Boolean>) -> Unit = {}
) {
    val items = listOf(
        "한번도 천체관측을 해본적이 없다.",
        "한두번 별을 본적이 있다.",
        "망원경을 사용하여 본적이 있다.",
        "별보는것이 이미 취미다."
    )
    var checks by rememberSaveable { mutableStateOf(List(items.size) { false }) }
    val canSubmit = checks.any { it }

    OnboardingScaffold(
        illustration = R.drawable.byeoldori,
        body = {
            Text("본인이 생각하는 정도에 체크해줘!!",
                color = Color.White, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            items.forEachIndexed { i, label ->
                AgreementCheckBox(
                    checked = checks[i],
                    onCheckedChange = { v ->
                        checks = checks.toMutableList().also { it[i] = v }
                    },
                    text = label,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottom = {
            WideButton(
                text = "확인",
                onClick = { onDone(checks) },
                enabled = canSubmit
            )
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ProfileStepPreview(){
    ProfileStep()
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ExperienceStepPreview(){
    ExperienceStep()
}
