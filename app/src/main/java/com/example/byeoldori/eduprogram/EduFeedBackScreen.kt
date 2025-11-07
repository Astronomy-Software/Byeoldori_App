package com.example.byeoldori.eduprogram

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.ui.theme.TextNormal

@Composable
fun EduFeedbackScreen() {
    val vm: EduFeedbackViewModel = hiltViewModel()

    // ✅ Local State 대신 -> ViewModel에서 상태 읽기
    var rating by remember { mutableStateOf(vm.rating) }
    var goodText by remember { mutableStateOf(vm.goodText) }
    var badText by remember { mutableStateOf(vm.badText) }

    // ✅ UI 변화 → ViewModel에도 즉시 반영
    fun updateRating(value: Int) {
        rating = value
        vm.updateRating(value)
    }

    fun updateGood(v: String) {
        goodText = v
        vm.updateGood(v)
    }

    fun updateBad(v: String) {
        badText = v
        vm.updateBad(v)
    }

    Background {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)

        ) {
            Text(
                text = "교육 프로그램 평가 남기기",
                style = MaterialTheme.typography.headlineSmall,
                color = TextHighlight
            )

            // ✅ 별점
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("교육 프로그램 별점",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextNormal
                )
                StarRating(
                    rating = rating,
                    onRatingChange = { updateRating(it) }
                )
            }

            // ✅ 좋았던 점
            OutlinedTextField(
                value = goodText,
                onValueChange = { updateGood(it) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("좋았던 점을 알려주세요", color = TextNormal) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextNormal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TextHighlight,
                    unfocusedBorderColor = TextNormal.copy(alpha = 0.6f),
                    focusedLabelColor = TextHighlight,
                    unfocusedLabelColor = TextNormal
                )
            )

            // ✅ 아쉬웠던 점
            OutlinedTextField(
                value = badText,
                onValueChange = { updateBad(it) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("아쉬웠던 점을 남겨주세요", color = TextNormal) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextNormal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TextHighlight,
                    unfocusedBorderColor = TextNormal.copy(alpha = 0.6f),
                    focusedLabelColor = TextHighlight,
                    unfocusedLabelColor = TextNormal
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ 버튼 영역
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = {vm.consumeFeedbackNavigation()}) {
                    Text("취소", color = TextNormal)
                }

                Button(
                    enabled = rating > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple500,
                        contentColor = TextNormal
                    ),
                    onClick = { vm.submitFeedback() }
                ) {
                    Text("제출하기")
                }
            }
        }
    }
}

@Composable
fun StarRating(
    rating: Int,
    maxRating: Int = 5,
    onRatingChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 1..maxRating) {
            IconButton(
                onClick = { onRatingChange(i) }
            ) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = null,
                    tint = if (i <= rating) Purple500 else TextNormal
                )
            }
        }
    }
}