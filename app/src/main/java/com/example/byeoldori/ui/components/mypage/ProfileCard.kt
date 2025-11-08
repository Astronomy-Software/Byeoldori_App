package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.ui.theme.*

@Composable
fun ProfileCard(
    name: String,
    observationCount: Int,
    onEditProfile: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenProfile() },
        colors = CardDefaults.cardColors(containerColor = Blue800),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.byeoldori),
                contentDescription = "프로필",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileContent(
    onBack: () -> Unit,
    userVm: UserViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { userVm.getMyProfile() }

    val me by userVm.userProfile.collectAsState()

    Column(Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "프로필",
                    color = TextHighlight,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.3f
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_before),
                        contentDescription = "뒤로가기",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
        HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 2.dp)
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 프로필 이미지 카드
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.byeoldori), // 여기에 기본 이미지 지정
                    contentDescription = "프로필 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                )
            }
            Text(
                text = me?.nickname?.takeIf { it.isNotBlank() } ?: (me?.name ?: "익명"),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                color = TextHighlight
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LabeledActionRow(label = "닉네임", value = me?.nickname ?: "—")
                LabeledActionRow(label = "이메일", value = me?.email ?: "—")
                LabeledActionRow(label = "전화번호", value = me?.phone ?: "—")
                LabeledActionRow(label = "생년월일", value = me?.birthdate ?: "—")
//                LabeledActionRow(
//                    label = "권한",
//                    value = (me?.roles ?: emptyList()).joinToString(", ").ifBlank { "—" }
//                )
//                LabeledActionRow(label = "마지막 로그인", value = me?.lastLoginAt ?: "—")
//                LabeledActionRow(label = "가입일", value = me?.createdAt ?: "—")
//                LabeledActionRow(label = "정보 수정일", value = me?.updatedAt ?: "—")
            }
        }
    }
}

@Composable
private fun LabeledActionRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {},
            enabled = enabled,
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple500,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            color = TextHighlight
        )
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
            onOpenProfile = {}
        )
    }
}