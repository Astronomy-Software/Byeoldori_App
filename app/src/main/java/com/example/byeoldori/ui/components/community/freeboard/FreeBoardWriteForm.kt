package com.example.byeoldori.ui.components.community.freeboard

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.common.copyUriToCache
import com.example.byeoldori.domain.Community.FreePost
import com.example.byeoldori.domain.Content
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.mapper.toUi
import com.example.byeoldori.ui.theme.Purple600
import com.example.byeoldori.viewmodel.*
import com.example.byeoldori.viewmodel.Community.*
import kotlinx.coroutines.launch

@Composable
fun FreeBoardWriteForm (
    onCancel: () -> Unit,
    onSubmit: () -> Unit,            // 등록
    onTempSave: () -> Unit,          // 임시 저장
    onMore: () -> Unit,
    onSubmitPost: (FreePost) -> Unit,
    initialPost: FreePost? = null,
    vm: CommunityViewModel? = null,
    onClose: () -> Unit
) {
    val isEditMode = initialPost != null
    var showCancelDialog by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf(initialPost?.title ?: "") }
    var pendingOnPicked by remember { mutableStateOf<((List<Uri>) -> Unit)?>(null) }
    var items by remember(initialPost?.id) {
        mutableStateOf(
            initialPost?.contentItems?.toUi()   // 도메인 → UI 변환
                ?: emptyList()
        )
    }
    LaunchedEffect(initialPost?.id) {
        if (items.none { it is EditorItem.Paragraph }) {
            items = items + EditorItem.Paragraph()
        }
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fileUploadVm: FileUploadViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        fileUploadVm.reset() // 이전 Success/Error 잔상 제거
    }

    val createState by (vm?.createState?.collectAsState()
        ?: remember { mutableStateOf<UiState<Any>>(UiState.Idle) })

    var uploadItems by remember(initialPost?.id) {
        mutableStateOf(
            if (isEditMode)
                initialPost!!.contentItems
                    .filterIsInstance<Content.Image.Url>()
                    .map { urlItem ->
                        UploadItem(
                            name = urlItem.url.substringAfterLast("/"),
                            url = urlItem.url,
                            status = UploadStatus.DONE,
                            sizeBytes = null,
                            progress = 1f
                        )
                    }
            else emptyList()

        )
    }
    val MAX_IMAGE_BYTES = 10L * 1024 * 1024 // 10MB
    val snackbar = remember { SnackbarHostState() }
    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris -> //uri리스트
        val picked = uris ?: emptyList()
        pendingOnPicked?.invoke(picked)
        pendingOnPicked = null

        picked.forEach { uri ->
            scope.launch {
                val file = copyUriToCache(context, uri) //URI → File 변환 함수
                fileUploadVm.reset()

                val afdLen = context.contentResolver
                    .openAssetFileDescriptor(uri,"r")
                    ?.length
                    ?: -1L
                val size = if(afdLen > 0) afdLen else file.length()
                if (size > MAX_IMAGE_BYTES) {
                    snackbar.showSnackbar("이미지 '${file.name}'이(가) 10MB를 초과하여 업로드할 수 없습니다.")
                    return@launch
                }

                uploadItems = uploadItems + UploadItem(
                    name = file.name,
                    status = UploadStatus.UPLOADING,
                    sizeBytes = size,
                    progress = 0f
                )
                val fileName = file.name
                fileUploadVm.uploadImage(file) { sent, total ->
                    val progress = if (total > 0) (sent.toFloat() / total).coerceIn(0f,1f) else 0f
                    uploadItems = uploadItems.toMutableList().also { list ->
                        val idx = list.indexOfFirst { it.name == fileName && it.status == UploadStatus.UPLOADING }
                        if (idx >= 0) list[idx] = list[idx].copy(progress = progress)
                    }

                } //업로드 시작
            }
        }
    }
    var uploadedImageUrls by remember {
        mutableStateOf(
            initialPost?.contentItems //기존 이미지 있으면 미리 URL로 채워두기
                ?.filterIsInstance<Content.Image.Url>()
                ?.map { it.url }
                ?: emptyList()
        )
    }

    //업로드 성공 시 URL을 items/리스트에 반영
    val uploadState by fileUploadVm.uploadState.collectAsState()
    LaunchedEffect(uploadState) {
        when (val s = uploadState) {
            is UiState.Success -> {
                val url = s.data
                // 진행 중이던 항목을 DONE으로 교체
                val idx = uploadItems.indexOfFirst { it.status == UploadStatus.UPLOADING }
                if (idx >= 0) {
                    val m = uploadItems.toMutableList()
                    m[idx] = m[idx].copy(url = url, status = UploadStatus.DONE, progress = 1f)
                    uploadItems = m
                }
                // 본문 items에도 사진 블록 추가
                items = items + EditorItem.Photo(model = url)
                // payload용 URL 누적
                uploadedImageUrls = uploadedImageUrls + url

                fileUploadVm.reset()
            }
            is UiState.Error -> {
                // 실패 표시
                val idx = uploadItems.indexOfFirst { it.status == UploadStatus.UPLOADING }
                if (idx >= 0) {
                    val m = uploadItems.toMutableList()
                    m[idx] = m[idx].copy(status = UploadStatus.ERROR)
                    uploadItems = m
                }
                snackbar.showSnackbar(s.message ?: "이미지 업로드에 실패했습니다.")
                fileUploadVm.reset()
            }
            else -> Unit
        }
    }

    fun buildContentText(list: List<EditorItem>) =
        list.filterIsInstance<EditorItem.Paragraph>()
            .joinToString("\n") { it.value.text }

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
                        val body = buildContentText(items).trim()
                        if (title.isBlank() || body.isBlank()) {
                            // 유효성 경고 다이얼로그 띄우기 등
                            showValidationDialog = true
                            return@WriteBar
                        }

                        if(isEditMode) {
                            val idL = initialPost!!.id.toLong()
                            vm?.updatePost(
                                postId = idL,
                                title = title.trim(),
                                content = body,
                                imageUrls = uploadedImageUrls,
                                onDone = onSubmit
                            )
                        } else {
                            vm?.createPost(
                                title = title.trim(),
                                content = body,
                                imageUrls = uploadedImageUrls
                            )
                            onSubmit()
                        }
                    },
                    onTempSave = { scope.launch { snackbar.showSnackbar("아직 준비중인 기능입니다",duration = SnackbarDuration.Short)} },
                    onCancel = { showCancelDialog = true },
                    onMore = { scope.launch { snackbar.showSnackbar("아직 준비중인 기능입니다",duration = SnackbarDuration.Short)} },
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
                    uploadItems = uploadItems,
                    onPickImages = { onPicked ->
                        pendingOnPicked = onPicked
                        pickImages.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onCheck = { scope.launch { snackbar.showSnackbar("아직 준비중인 기능입니다",duration = SnackbarDuration.Short)} },
                    onChecklist = { scope.launch { snackbar.showSnackbar("아직 준비중인 기능입니다",duration = SnackbarDuration.Short)} },
                    readOnly = false
                )
            }
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                containerColor = Purple600,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(data.visuals.message)
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
    when (val s = createState) {
        UiState.Loading -> {
            CircularProgressIndicator()
        }
        is UiState.Success -> {
            LaunchedEffect(s.data) {
                vm?.clearCreateState()
                onSubmit()
                //onClose()
            }
        }
        is UiState.Error -> {
            Log.e("FreeBoardWriteForm", "게시글 저장 실패: ${s.message}")
            vm?.clearCreateState()
        }
        else -> Unit
    }
    if (showValidationDialog) {
        AlertDialog(
            onDismissRequest = { showValidationDialog = false },
            text = { Text("필수 항목을 모두 입력해주세요.") },
            confirmButton = {
                TextButton(onClick = { showValidationDialog = false }) { Text("확인") }
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720, backgroundColor = 0xFF241860)
@Composable
private fun Preview_FreeBoardWriteForm() {
    MaterialTheme {
        FreeBoardWriteForm(
            onCancel = {},
            onSubmit = {},
            onTempSave = {},
            onMore = {},
            onSubmitPost = {},
            initialPost = null,
            onClose = {}
        )
    }
}