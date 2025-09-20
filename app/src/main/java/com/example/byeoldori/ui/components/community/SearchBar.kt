package com.example.byeoldori.ui.components.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*

@Composable
fun SearchBar(
    searchQuery: String,
    onSearch: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // 검색창
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearch, //키보드로 입력할 때마다 호출
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = TextHighlight
                )
            },
            placeholder = { Text("검색할 내용을 입력해주세요", color = TextHighlight) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Blue800, //검색창 터치한 후의 색상
                unfocusedContainerColor = Blue800,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Preview(name = "SearchBar", showBackground = true, backgroundColor = 0xFF241860)
@Composable
private fun Preview_SearchBar() {
    MaterialTheme {
        SearchBar(
            searchQuery = "",
            onSearch = {}
        )
    }
}

