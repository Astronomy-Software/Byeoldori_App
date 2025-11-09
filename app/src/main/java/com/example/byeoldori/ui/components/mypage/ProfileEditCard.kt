package com.example.byeoldori.ui.components.mypage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.byeoldori.R
import com.example.byeoldori.data.UserUiState
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.data.model.dto.UpdateUserProfile
import com.example.byeoldori.ui.theme.TextHighlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditCard(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    userVm: UserViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { userVm.getMyProfile() }
    val me = userVm.userProfile.collectAsState().value

    var nickname by rememberSaveable(me?.nickname) { mutableStateOf(me?.nickname.orEmpty()) }
    var phone by rememberSaveable(me?.phone) { mutableStateOf(me?.phone.orEmpty()) }
    var birthdate by rememberSaveable(me?.birthdate) { mutableStateOf(me?.birthdate.orEmpty()) }

    val ui by userVm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var saving by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var pendingSave by remember { mutableStateOf(false) }

    val ctx = LocalContext.current
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            userVm.uploadProfileImage(ctx, uri)   // 업로드
        }
    }

    LaunchedEffect(ui) {
        when (ui) {
            is UserUiState.Loading -> {
                // 저장 중일 때만 로딩 UI 반영
                if (pendingSave) saving = true
            }
            is UserUiState.Success<*> -> {
                //저장 성공일 때만 다이얼로그
                if (pendingSave) {
                    saving = false
                    showSuccessDialog = true
                    pendingSave = false
                } else {
                    // 프로필 조회 성공 등은 무시
                    saving = false
                }
            }
            is UserUiState.Error -> {
                if (pendingSave) {
                    saving = false
                    snackbarHostState.showSnackbar((ui as UserUiState.Error).message)
                    pendingSave = false
                }
            }
            else -> Unit
        }
    }

    Column(Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "프로필 편집",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = me?.profileImageUrl,
                    placeholder = painterResource(id = R.drawable.byeoldori),
                    error = painterResource(id = R.drawable.byeoldori),
                    contentDescription = "프로필 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
                IconButton(
                    onClick = { pickImage.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = 40.dp, y = 6.dp)
                        .size(40.dp)
                        .border(2.dp, Color.White, CircleShape)
                        .padding(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit), // R.drawable 아이콘 사용
                        contentDescription = "수정",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("닉네임",color = Color.White) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("전화번호", color = Color.White) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = birthdate,
                onValueChange = { birthdate = it },
                label = { Text("생년월일 (yyyy-MM-dd)",color = Color.White) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // 액션 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) { Text("취소") }

                Button(
                    onClick = {
                        val body = UpdateUserProfile(
                            nickname = nickname.takeIf { it.isNotBlank() },
                            phone = phone.takeIf { it.isNotBlank() },
                            birthdate = birthdate.takeIf { it.isNotBlank() }
                        )
                        pendingSave = true
                        userVm.updateMe(body)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !saving
                )  {
                    if (saving) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("저장")
                }
            }
        }
    }
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* 바깥 터치로 닫히지 않게 하려면 비워두기 */ },
            title = { Text("프로필 수정", color = Color.Black) },
            text = { Text("프로필이 수정되었습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onSaved() // 확인 시 상위 화면 흐름(예: 상세 화면으로 전환)
                }) {
                    Text("확인")
                }
            }
        )
    }
}