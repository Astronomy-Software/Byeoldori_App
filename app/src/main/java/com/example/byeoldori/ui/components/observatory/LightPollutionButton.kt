package com.example.byeoldori.ui.components.observatory


import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.ui.theme.*

@Composable
fun LightPollutionButton(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = { onCheckedChange(!checked) },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (checked) Blue500 else Purple500,
            contentColor = TextHighlight
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(50.dp)
    ) {
        Text(if (checked) "광공해 끄기" else "광공해 보기", fontSize = 12.sp)
    }
}

@Preview(showBackground = true, name = "LightPollutionButton - 켜짐")
@Composable
private fun Preview_LightPollutionButton_On() {
    Surface(color = Color.Black) {
        LightPollutionButton(
            checked = true,
            onCheckedChange = {}
        )
    }
}

@Preview(showBackground = true, name = "LightPollutionButton - 꺼짐")
@Composable
private fun Preview_LightPollutionButton_Off() {
    Surface(color = Color.Black) {
        LightPollutionButton(
            checked = false,
            onCheckedChange = {}
        )
    }
}
















