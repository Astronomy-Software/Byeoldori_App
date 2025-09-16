package com.example.byeoldori.ui.components.community

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*

@Composable
fun CommentInput(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit = {},
    placeholder: String = "댓글을 입력해주세요",
    enabled: Boolean = true
) {
    val focus = LocalFocusManager.current
    val canSend = text.isNotBlank() && enabled
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Purple900),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder, color = TextDisabled) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),//엔터키 줄바꿈으로
            singleLine = false,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = TextHighlight
            )
        )
        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (canSend) {
                    onSend(text)
                    focus.clearFocus()
                }
            },
            enabled = canSend
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_send),
                contentDescription = "전송",
                tint = if (canSend) TextHighlight else TextDisabled
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF241860)
private fun Preview_CommentInput() {
    var text by rememberSaveable { mutableStateOf("") }
    MaterialTheme {
        CommentInput(
            text = text,
            onTextChange = { text = it },
            onSend = { /* 전송 테스트 */ }
        )
    }
}