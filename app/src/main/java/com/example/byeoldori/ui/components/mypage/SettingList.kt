package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingList(
    onBack: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenLicenses: () -> Unit = {}
) {
    var allowLocation by rememberSaveable { mutableStateOf(true) }
    var allowPush by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Background(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "설정",
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    MenuGroupCard(
                        containerColor = Blue800,
                        items = listOf(
                            MenuItem(
                                title = "위치정보 제공 허용",
                                onClick = { allowLocation = !allowLocation },
                                trailing = {
                                    Switch(
                                        checked = allowLocation,
                                        onCheckedChange = { allowLocation = it }
                                    )
                                }
                            ),
                            MenuItem(
                                title = "푸시 알림 허용",
                                onClick = { allowPush = !allowPush },
                                trailing = {
                                    Switch(
                                        checked = allowPush,
                                        onCheckedChange = { allowPush = it }
                                    )
                                }
                            ),
                            MenuItem(
                                title = "버전정보",
                                trailing = {
                                    Text("v1.0.0", color = TextHighlight)
                                },
                                onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "아직 준비중인 기능입니다",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            ),
                            MenuItem(
                                "이용약관",
                                onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "아직 준비중인 기능입니다",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            ),
                            MenuItem(
                                "오픈소스 목록 및 라이센스",
                                onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "아직 준비중인 기능입니다",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
                        )
                    )
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .align(Alignment.BottomCenter)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Purple500,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 400, heightDp = 900)
@Composable
private fun Preview_SettingsScreen() {
    MaterialTheme {
        SettingList()
    }
}