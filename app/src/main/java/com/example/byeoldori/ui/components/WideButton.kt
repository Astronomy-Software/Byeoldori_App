package com.example.byeoldori.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.ui.theme.TextNormal

@Composable
fun WideButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Purple500,
    contentColor: Color = TextNormal,
    icon: Painter? = null,
    iconDescription: String? = null,
    cornerRadius: Dp = 8.dp,
    height: Dp = 56.dp,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(cornerRadius),
        contentPadding = PaddingValues(horizontal = 16.dp)  // 내부 여백
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = iconDescription,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
            }
            Text(
                text = text,
                fontSize = 10.sp,
                style = textStyle,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WideButtonPreview() {
    Column(){
        WideButton(
            text = "미리보기 버튼",
            onClick = {},
            icon = null
        )
        WideButton(
            text = "미리보기 버튼",
            onClick = {},
            icon = null,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        WideButton(
            text = "미리보기 버튼",
            onClick = {},
            icon = null,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
        )
        WideButton(
            text = "구글 로그인",
            onClick = {},
            icon = rememberAsyncImagePainter("https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg"),
            iconDescription = "Google Icon"
        )
    }
}

