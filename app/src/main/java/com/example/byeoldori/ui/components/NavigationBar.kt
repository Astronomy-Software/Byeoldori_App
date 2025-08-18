package com.example.byeoldori.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.ui.theme.Purple800
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.ui.theme.TextNormal

@Composable
fun NavigationBar(
    onItemSelected: (String) -> Unit,
    selectedItem: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Purple800)
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val items = listOf(
            Triple(R.drawable.ic_star, "별지도", "별지도"),
            Triple(R.drawable.ic_location_on, "관측지", "관측지"),
            Triple(R.drawable.ic_home, "홈", "홈"),
            Triple(R.drawable.ic_groups, "커뮤니티", "커뮤니티"),
            Triple(R.drawable.ic_account_circle, "마이페이지", "마이페이지")
        )

        items.forEach { (iconRes, label, key) ->
            val isSelected = selectedItem == key
            val backgroundColor = if (isSelected) Purple500 else Color.Transparent
            Box(
                modifier = Modifier
                    .clickable { onItemSelected(key) }
                    .background(backgroundColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                    .padding(horizontal = 0.dp, vertical = 2.dp)
                    .width(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        modifier = Modifier
                            .size(30.dp)
                            .offset(y=5.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        color = if (isSelected) TextHighlight else TextNormal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFF5F5F7,
    widthDp = 360,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun NavigationBarPreview() {
    var selected = remember { mutableStateOf("홈") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(640.dp)
            .background(Color(0xFF120A2A))
    ) {
        // 상단 더미 콘텐츠 공간
        Spacer(modifier = Modifier.weight(1f))

        // 하단 네비게이션 바
        NavigationBar(
            selectedItem = selected.value,
            onItemSelected = { selected.value = it }
        )
    }
}

