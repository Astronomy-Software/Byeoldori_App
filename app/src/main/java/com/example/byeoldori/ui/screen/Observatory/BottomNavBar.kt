package com.example.byeoldori.ui.screen.Observatory

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.byeoldori.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavBar(
    onItemSelected: (String) -> Unit,
    selectedItem: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF48287B))
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val items = listOf(
            Triple(R.drawable.menu_star, "별지도", "별지도"),
            Triple(R.drawable.menu_obs, "관측지", "관측지"),
            Triple(R.drawable.menu_home, "홈", "홈"),
            Triple(R.drawable.menu_com, "커뮤니티", "커뮤니티"),
            Triple(R.drawable.menu_my, "마이페이지", "마이페이지")
        )

        items.forEach { (iconRes, label, key) ->
            val isSelected = selectedItem == key
            val backgroundColor = if (isSelected) Color(0xFF8459C9) else Color.Transparent
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { onItemSelected(key) }
                    .background(backgroundColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 2.dp), // 내부 여백
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
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

