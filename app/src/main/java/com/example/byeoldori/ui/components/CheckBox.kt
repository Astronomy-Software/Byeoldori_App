package com.example.byeoldori.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.ui.theme.TextHighlight

@Composable
fun AgreementCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    text: String = "개인정보 처리방침 전체 동의 (필수)"
) {
    val borderColor =  Purple500
    val backgroundColor = TextHighlight
    val checkboxColor = Purple500
    val width = 330.dp

    Box(
        modifier = modifier
            .width(width)
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 10.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Purple500,
                    fontWeight = FontWeight.Bold
                )
            )
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = checkboxColor,
                    uncheckedColor = checkboxColor,
                    checkmarkColor = TextHighlight,
                )
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AgreementCheckBoxPreview() {
    var checked by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        AgreementCheckBox(
            checked = checked,
            onCheckedChange = { checked = it }
        )

        AgreementCheckBox(
            checked = checked,
            text = "안녕하세요 저는 긴 문장을 만들 예정입니다. 다음과 같이 별도리는 정말 귀여운 생물입니다.",
            onCheckedChange = { checked = it }
        )
    }
}
