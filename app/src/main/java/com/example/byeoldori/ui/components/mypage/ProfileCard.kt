package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*

@Composable
fun ProfileCard(
    name: String,
    observationCount: Int,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Blue800),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile1),
                contentDescription = "프로필",
                tint = Color.Unspecified,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.width(12.dp))

            //이름 + 관측 횟수
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge.copy( // 기존 titleMedium → titleLarge
                            color = TextHighlight
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onEditProfile) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "수정",
                                tint = Color.White,
                                modifier = Modifier.size(25.dp)

                            )
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = "설정",
                                tint = Color.White,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "관측 횟수   ${observationCount}회",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDisabled)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 140)
@Composable
private fun PreviewProfileCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        ProfileCard(
            name = "별도리",
            observationCount = 123,
            onEditProfile = {},
            onOpenSettings = {}
        )
    }
}