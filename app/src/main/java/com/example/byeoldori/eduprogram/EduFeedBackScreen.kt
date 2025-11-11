package com.example.byeoldori.eduprogram

import android.graphics.Rect
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.ui.theme.TextNormal
import kotlinx.coroutines.launch

@Composable
fun EduFeedbackScreen() {
    val vm: EduFeedbackViewModel = hiltViewModel()

    var rating by remember { mutableStateOf(vm.rating) }
    var goodText by remember { mutableStateOf(vm.goodText) }
    var badText by remember { mutableStateOf(vm.badText) }

    val context = LocalContext.current
    val toastMessage by vm.toastMessage.collectAsState()
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.consumeToastMessage()
        }
    }

    val bringIntoViewRequester1 = remember { BringIntoViewRequester() }
    val bringIntoViewRequester2 = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val view = LocalView.current

    var isKeyboardVisible by remember { mutableStateOf(false) }

    // ✅ 키보드 열림 감지
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardVisible = keypadHeight > screenHeight * 0.15
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }

    // ✅ 안정적 구조: imePadding 제거 + verticalScroll 추가
    Background {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scrollState), // ✅ 스크롤 가능하도록
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // 제목
            Text(
                text = "교육 프로그램 평가",
                style = MaterialTheme.typography.headlineSmall,
                color = TextHighlight
            )

            // 별점
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "교육 프로그램 별점",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextNormal
                )
                StarRating(rating = rating, onRatingChange = { rating = it; vm.updateRating(it) })
            }

            // 좋았던 점 입력창
            OutlinedTextField(
                value = goodText,
                onValueChange = { vm.updateGood(it); goodText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(bringIntoViewRequester1)
                    .onFocusEvent { event ->
                        if (event.isFocused && isKeyboardVisible) {
                            coroutineScope.launch { bringIntoViewRequester1.bringIntoView() }
                        }
                    },
                label = { Text("좋았던 점을 알려주세요", color = TextNormal) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextNormal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TextHighlight,
                    unfocusedBorderColor = TextNormal.copy(alpha = 0.6f),
                    focusedLabelColor = TextHighlight,
                    unfocusedLabelColor = TextNormal
                )
            )

            // 아쉬웠던 점 입력창
            OutlinedTextField(
                value = badText,
                onValueChange = { vm.updateBad(it); badText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(bringIntoViewRequester2)
                    .onFocusEvent { event ->
                        if (event.isFocused && isKeyboardVisible) {
                            coroutineScope.launch { bringIntoViewRequester2.bringIntoView() }
                        }
                    },
                label = { Text("아쉬웠던 점을 남겨주세요", color = TextNormal) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextNormal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TextHighlight,
                    unfocusedBorderColor = TextNormal.copy(alpha = 0.6f),
                    focusedLabelColor = TextHighlight,
                    unfocusedLabelColor = TextNormal
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = { vm.consumeFeedbackNavigation() }) {
                    Text("취소", color = TextNormal)
                }
                Spacer(modifier = Modifier.width(12.dp))
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
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 1..maxRating) {
            IconButton(onClick = { onRatingChange(i) }) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = null,
                    tint = if (i <= rating) Purple500 else TextNormal
                )
            }
        }
    }
}
