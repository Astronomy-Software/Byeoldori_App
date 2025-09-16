package com.example.byeoldori.ui.components.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*

@Composable
fun TitleInput (
    title: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "제목을 입력해주세요"
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            //.offset(y=(10).dp)
    ) {
        BasicTextField(
            value = title,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            ),
            cursorBrush = SolidColor(Color.White), // 커서 색상 지정
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (title.isEmpty()) {
                    Text(
                        placeholder,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                innerTextField() // 실제 입력 영역
            }
        )
        Divider(
            color = Color.White.copy(alpha = 0.6f),
            thickness = 2.dp,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
        )
    }
}

@Preview(name = "TitleInput", showBackground = true, backgroundColor = 0xFF241860, widthDp = 500)

@Composable
private fun Preview_TextInput() {
    MaterialTheme {
        var title by rememberSaveable { mutableStateOf("") }
        TitleInput(
            title = title,
            onValueChange = { title = it }
        )
    }
}