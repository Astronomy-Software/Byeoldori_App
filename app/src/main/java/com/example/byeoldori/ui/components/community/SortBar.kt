package com.example.byeoldori.ui.components.community

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.ui.components.community.review.ReviewSort
import com.example.byeoldori.ui.theme.*

//선택 상태
@Composable
private fun sortTextColor(selected: Boolean) =
    if (selected) TextHighlight else TextDisabled

//정렬 바
@Composable
fun <T> SortBar(
    current: T,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { option ->
            Text("•", color = TextDisabled, modifier = Modifier.padding(horizontal = 8.dp))
            Text(
                text = label(option),
                color = sortTextColor(current == option),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onSelect(option) }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860)
@Composable
private fun Preview_SortBar() {
    var selected by remember { mutableStateOf(ReviewSort.Latest) }

    MaterialTheme {
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blue800)
                    .padding(12.dp)
            ) {
                SortBar(
                    current = selected,
                    options = ReviewSort.entries.toList(),
                    label = { it.label },
                    onSelect = { selected = it }
                )
            }
        }
    }
}
