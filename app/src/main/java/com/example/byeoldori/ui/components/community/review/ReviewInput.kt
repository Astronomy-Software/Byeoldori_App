package com.example.byeoldori.ui.components.community.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.ui.components.community.toShortDate
import com.example.byeoldori.ui.theme.TextDisabled
import com.example.byeoldori.ui.theme.TextHighlight

@Composable
fun ReviewInput (
    target: String,
    onTargetChange: (String) -> Unit,
    site: String,
    onSiteChange: (String) -> Unit,
    equipment: String,
    onEquipmentChange: (String) -> Unit,
    date: String,
    onDateChange: (String) -> Unit,
    onTimeChange: (String, String) -> Unit,
    rating: String,
    onRatingChange: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Box(Modifier.weight(1f).fillMaxHeight()) {
                Label(
                    title = "관측 대상",
                    value = target,
                    onValueChange = onTargetChange,
                    placeholder = "관측 대상을 선택해주세요",
                    enabled = enabled
                )
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                Label(
                    title = "관측지",
                    value = site,
                    onValueChange = onSiteChange,
                    placeholder = "관측지를 선택해주세요",
                    enabled = enabled
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Box(Modifier.weight(1f).fillMaxHeight()) {
                Label(
                    title = "관측 장비",
                    value = equipment,
                    onValueChange = onEquipmentChange,
                    placeholder = "관측 장비를 입력해주세요",
                    enabled = enabled
                )
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                if(enabled) {
                    DateSelection(
                        label = "관측 일자",
                        date = date,
                        onPicked = { pickedDate ->
                            onDateChange(pickedDate)
                        }
                    )
                } else {
                    //읽기 모드
                    Label(
                        title = "관측 일자",
                        value = if (date.isBlank()) "" else "${date.toShortDate()}",
                        placeholder = "관측 일자를 선택해주세요",
                        selectable = false,
                        enabled = false
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Box(Modifier.weight(1f).fillMaxHeight()) {
                Label(
                    title = "관측 평점",
                    value = rating,
                    selectable = true,
                    placeholder = "관측 평점을 입력해주세요",
                    onClick = onRatingChange,
                    enabled = enabled
                )
            }
        }
        Divider(
            color = Color.White.copy(alpha = 0.6f),
            thickness = 2.dp,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
        )
    }
}

//관측 정보 작성 부분 (제목이랑 값 위치 정렬)
@Composable
fun Label(
    title: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    placeholder: String,
    selectable: Boolean = false,
    onClick: () -> Unit = {},
    enabled: Boolean = true
) {
    Column {
        Text(title, color = TextDisabled, style = MaterialTheme.typography.titleSmall, fontSize = 14.sp)

        if (selectable) {
            val clickableMod = if (enabled) {
                Modifier.clickable { onClick() }
            } else {
                Modifier // 클릭 막음
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 30.dp)
                    .padding(vertical = 8.dp)
                    .then(clickableMod)
            ) {
                if(value.isEmpty()) {
                    Text(placeholder, color = TextDisabled.copy(alpha = 0.5f), fontSize = 14.sp)
                } else {
                    Text(value, color = TextHighlight, fontSize = 14.sp)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 30.dp)
                    .padding(vertical = 8.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    readOnly = !enabled,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = TextHighlight
                    ),
                    cursorBrush = SolidColor(TextHighlight),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                fontSize = 14.sp,
                                color = TextDisabled.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 500)
@Composable
private fun Preview_ReviewInputSection() {
    MaterialTheme {
        var target by rememberSaveable { mutableStateOf("") }
        var site by rememberSaveable { mutableStateOf("") }
        var equipment by rememberSaveable { mutableStateOf("") }
        var start by rememberSaveable { mutableStateOf("") }
        var end by rememberSaveable { mutableStateOf("") }
        var rating by rememberSaveable { mutableStateOf("") }

        ReviewInput(
            target = target,
            onTargetChange = { target = it },
            site = site,
            onSiteChange = { site = it },
            equipment = equipment,
            onEquipmentChange = { equipment = it },
            date = "",
            onTimeChange = { s, e -> start = s; end = e },
            rating = rating,
            onRatingChange = {},
            onDateChange = {},
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
        )
    }
}
