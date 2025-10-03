package com.example.byeoldori.ui.components.community.freeboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.domain.Community.FreePost
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.mapper.toDomain
import com.example.byeoldori.ui.mapper.toUi

@Composable
fun FreeBoardWriteForm (
    author: String,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,            // 등록
    onTempSave: () -> Unit,          // 임시 저장
    onMore: () -> Unit,
    now: () -> Long = { System.currentTimeMillis() },
    onSubmitPost: (FreePost) -> Unit,
    initialPost: FreePost? = null //?
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var pendingOnPicked by remember { mutableStateOf<((List<Uri>) -> Unit)?>(null) }

    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        // ContentInput이 넘겨준 onPicked를 여기서 호출
        pendingOnPicked?.invoke(uris ?: emptyList())
        pendingOnPicked = null
    }
    var items by remember {
        mutableStateOf(
            initialPost?.contentItems?.toUi()   // 도메인 → UI 변환
                ?: listOf<EditorItem>(EditorItem.Paragraph())
        )
    }

    fun makePost(): FreePost {
        val createdAt = now()
        return FreePost(
            id = createdAt.toString(),
            title = title,
            author = author,
            profile = R.drawable.profile1,
            likeCount = 0,
            commentCount = 0,
            viewCount = 0,
            createdAt = createdAt,
            contentItems = items.toDomain()
        )
    }
    fun validate(): Boolean {
        val hasTitle = title.isNotBlank()
        val hasContent = items.any { it is EditorItem.Paragraph && it.value.text.isNotBlank() }
        return hasTitle && hasContent
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .offset(y = 10.dp)
                .advancedImePadding()
        ) {
            item {
                WriteBar(
                    onSubmit = {
                        if (validate()) {
                            onSubmitPost(makePost())
                            onSubmit()
                        } else {
                            showValidationDialog = true
                        }
                    },
                    onTempSave = onTempSave,
                    onCancel = { showCancelDialog = true },
                    onMore = onMore
                )
            }
            item {
                Divider(
                    color = Color.White.copy(alpha = 0.6f),
                    thickness = 1.dp,
                    modifier = Modifier.offset(y = (-15).dp)
                )
            }
            item {
                TitleInput(
                    title = title,
                    onValueChange = { title = it }
                )
            }
            item {
                ContentInput(
                    items = items,
                    onItemsChange = { items = it },
                    onPickImages = { onPicked ->
                        pendingOnPicked = onPicked
                        pickImages.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onCheck = {},
                    onChecklist = { /* 자유게시판이면 생략 or 원하는 동작 */ }
                )
            }
        }
        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                text = { Text("작성 중인 내용을 취소하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        showCancelDialog = false
                        onCancel()                 // ← 실제 뒤로가기 콜백 호출
                    }) { Text("예") }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) { Text("아니오") }
                }
            )
        }
        if (showValidationDialog) {
            AlertDialog(
                onDismissRequest = { showValidationDialog = false },
                text = { Text("필수항목을 모두 입력해주세요.") },
                confirmButton = {
                    TextButton(onClick = { showValidationDialog = false }) { Text("확인") }
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720, backgroundColor = 0xFF241860)
@Composable
private fun Preview_FreeBoardWriteForm() {
    MaterialTheme {
        FreeBoardWriteForm(
            author = "astro_user",
            onCancel = {},
            onSubmit = {},
            onTempSave = {},
            onMore = {},
            onSubmitPost = {}
        )
    }
}