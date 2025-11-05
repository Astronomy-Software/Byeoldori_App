package com.example.byeoldori.ui.components.mypage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.common.copyUriToCache
import com.example.byeoldori.data.model.dto.CreatePlanRequest
import com.example.byeoldori.data.model.dto.EventStatus
import com.example.byeoldori.data.model.dto.PlanDetailDto
import com.example.byeoldori.data.model.dto.UpdatePlanRequest
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.theme.Purple600
import com.example.byeoldori.viewmodel.Community.FileUploadViewModel
import com.example.byeoldori.viewmodel.Community.PlanViewModel
import com.example.byeoldori.viewmodel.UiState
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private fun parseDateTimeFlexible(raw: String): LocalDateTime {
    runCatching { return ZonedDateTime.parse(raw).toLocalDateTime() }
    runCatching { return OffsetDateTime.parse(raw).toLocalDateTime() }
    runCatching { return LocalDateTime.parse(raw) }
    val noSec = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    runCatching { return LocalDateTime.parse(raw, noSec) }
    return LocalDateTime.now()
}

private fun makeDateFieldValue(startAt: String, endAt: String): String {
    val s = parseDateTimeFlexible(startAt)
    val e = parseDateTimeFlexible(endAt)
    val d = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val t = DateTimeFormatter.ofPattern("HH:mm")
    return "${s.format(d)} ${s.format(t)} ${e.format(t)}"
}

@Composable
fun PlanWriteForm(
    onBack: () -> Unit = {},
    planVm: PlanViewModel,
    initialPlan: PlanDetailDto? = null
) {
    val scope = rememberCoroutineScope()
    var showSavedDialog by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    var editorItems by remember { mutableStateOf<List<EditorItem>>(emptyList()) }
    var uploadItems by remember { mutableStateOf<List<UploadItem>>(emptyList()) }
    var pendingOnPicked by remember { mutableStateOf<((List<Uri>) -> Unit)?>(null) }

    val fileUploadVm: FileUploadViewModel = hiltViewModel()
    LaunchedEffect(Unit) { fileUploadVm.reset() }
    val uploadState by fileUploadVm.uploadState.collectAsState()
    val context = LocalContext.current

    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> -> //uri리스트
        val picked = uris  //아무것도 안골랐을 수도 있으니 nullable

        // ContentInput이 넘겨준 onPicked를 여기서 호출
        pendingOnPicked?.invoke(picked)
        pendingOnPicked = null

        val MAX_IMAGE_BYTES = 10L * 1024 * 1024 // 10MB
        picked.forEach { uri ->
            scope.launch {//선택한 각 Uri를 순회하면서 코루틴으로 비동기 처리
                val file = copyUriToCache(context, uri) //URI → File 변환 함수
                fileUploadVm.reset()

                val afdLen = context.contentResolver //uRI가 가리키는 파일의 크기를 알아냄
                    .openAssetFileDescriptor(uri,"r") //시스템이 파일을 대신 열어줌
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

    var title by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("") }
    var site by rememberSaveable { mutableStateOf("") }
    var memo by rememberSaveable { mutableStateOf("") }

    val createState by planVm.createState.collectAsState()
    val updateState by planVm.updateState.collectAsState()

    //초기 내용 입력창 하나 생기도록
    LaunchedEffect(Unit) {
        if(editorItems.none { it is EditorItem.Paragraph }) {
            editorItems = editorItems + EditorItem.Paragraph()
        }
    }

    //임시 로컬 Uri 이미지를 서버 URL로 교체
    LaunchedEffect(uploadState) {
        when (val s = uploadState) {
            is UiState.Success -> {
                val url = s.data //업로드 성공 시, 서버에서 받은 URL을 꺼냄
                val idx = uploadItems.indexOfFirst { it.status == UploadStatus.UPLOADING }
                if (idx >= 0) {
                    val m = uploadItems.toMutableList()
                    m[idx] = m[idx].copy(url = url, status = UploadStatus.DONE, progress = 1f)
                    uploadItems = m
                }
                editorItems =  editorItems + EditorItem.Photo(model = url)
                fileUploadVm.reset() //다음 이미지 업로드를 준비하기 위해 Idle로 초기화
            }
            is UiState.Error -> {
                // 실패한 항목 표시
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

    LaunchedEffect(initialPlan) {
        initialPlan ?: return@LaunchedEffect
        title = initialPlan.title.orEmpty()
        date = makeDateFieldValue(initialPlan.startAt, initialPlan.endAt)
        target = initialPlan.targets?.filter { it.isNotBlank() }?.joinToString(", ") ?: initialPlan.title.orEmpty()
        site = initialPlan.placeName ?: (initialPlan.observationSiteName ?: "")
        memo = initialPlan.memo ?: ""
        val paragraph = if (memo.isNotBlank()) {
            EditorItem.Paragraph(value = TextFieldValue(memo))
        } else {
            EditorItem.Paragraph()
        }
        val photos = initialPlan.photos?.mapNotNull { it.url }?.map { EditorItem.Photo(model = it) } ?: emptyList()
        editorItems = listOf(paragraph) + photos
    }

    fun buildContentText(items: List<EditorItem>): String =
        items.joinToString("\n") { if (it is EditorItem.Paragraph) it.value.text else "" }

    fun collectImageUrls(items: List<EditorItem>): List<String> =
        items.filterIsInstance<EditorItem.Photo>()
            .mapNotNull { it.model as? String }

    fun parseDateTimes(raw: String): Pair<String, String> {
        // raw 예: "2025-11-05 20:50 23:10"
        val parts = raw.split(" ")
        return if (parts.size >= 3) {
            val startIso = "${parts[0]}T${parts[1]}"
            val endIso = "${parts[0]}T${parts[2]}"
            startIso to endIso
        } else {
            "" to ""
        }
    }

    LaunchedEffect(createState) {
        when (val s = createState) {
            is UiState.Success -> { showSavedDialog = true }
            is UiState.Error -> { snackbar.showSnackbar(s.message ?: "관측 계획 생성 실패") }
            else -> Unit
        }
    }
    LaunchedEffect(updateState) {
        when (val s = updateState) {
            is UiState.Success -> showSavedDialog = true
            is UiState.Error   -> snackbar.showSnackbar(s.message ?: "관측 계획 수정 실패")
            else -> Unit
        }
    }

    Box(Modifier.fillMaxSize()) {
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
                        val images = collectImageUrls(editorItems)
                        val content = buildContentText(editorItems).trim()

                        val (startIso, endIso) = parseDateTimes(date)
                        if (startIso.isBlank() || endIso.isBlank()) {
                            scope.launch { snackbar.showSnackbar("관측 일시를 선택해주세요") }
                            return@WriteBar
                        }
                        if (initialPlan == null) {
                            // ➤ 생성: 기존 그대로
                            val body = CreatePlanRequest(
                                title = if (title.isNotBlank()) title else "-",
                                startAt = startIso,
                                endAt = endIso,
                                observationSiteId = null,
                                targets = target.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                                lat = null, lon = null,
                                placeName = site.ifBlank { null },
                                memo = content.ifBlank { null },
                                imageUrls = images
                            )
                            planVm.createPlan(body)
                        } else {
                            val existing = initialPlan.photos?.mapNotNull { it.url } ?: emptyList()
                            val addOnly = images.filter { it !in existing }

                            val body = UpdatePlanRequest(
                                title = if (title.isNotBlank()) title else null,
                                startAt = startIso,
                                endAt = endIso,
                                targets = target.split(',').map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { null },
                                observationSiteId = null,
                                lat = null, lon = null,
                                placeName = site.ifBlank { null },
                                memo = content.ifBlank { null },
                                status = EventStatus.PLANNED, // 필요 시 유지/제거
                                addImageUrls = addOnly,
                                removeImageIds = emptyList()
                            )
                            planVm.updatePlan(initialPlan.id, body)
                        }
                    },
                    onTempSave = {
                        scope.launch {
                            snackbar.showSnackbar("아직 준비중인 기능입니다")
                        }
                    },
                    onCancel = { onBack() },
                    onMore = {
                        scope.launch {
                            snackbar.showSnackbar("아직 준비중인 기능입니다")
                        }
                    }
                )
            }
            item { Divider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp, modifier = Modifier.offset(y = (-15).dp)) }
            item { TitleInput(title = title, onValueChange = { title = it }, placeholder = "제목을 입력해주세요") }
            item { Divider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp, modifier = Modifier.offset(y = (-15).dp)) }

            item {
                PlanInputSection(
                    date = date,
                    onDateChange = { date = it },
                    target = target,
                    onTargetChange = { target = it },
                    site = site,
                    onSiteChange = { site = it },
                    memo = memo,
                    onMemoChange = { memo = it }
                )
            } //관측 일자, 관측 대상, 관측지

            item {
                Spacer(Modifier.height(8.dp))
                ContentInput(
                    items = editorItems,
                    onItemsChange = { editorItems = it },
                    uploadItems = uploadItems,
                    onPickImages = { onPicked ->
                        //scope.launch { snackbar.showSnackbar("아직 준비중인 기능입니다") }
                        pendingOnPicked = onPicked
                        pickImages.launch( //갤러리 오픈
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onCheck = {
                        scope.launch { snackbar.showSnackbar("아직 준비중인 기능입니다") }
                    },
                    onChecklist = {
                        scope.launch { snackbar.showSnackbar("아직 준비중인 기능입니다") }
                    },
                    readOnly = false,
                )
            }
        }
        if (showSavedDialog) {
            AlertDialog(
                onDismissRequest = { showSavedDialog = false },
                title = { Text("관측 계획 저장") },
                text = { Text("관측 계획이 저장되었습니다.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSavedDialog = false
                        planVm.resetCreateState()
                        planVm.resetUpdateState()
                        onBack() // 저장 후 이전 화면으로 복귀 (원치 않으면 제거)
                    }) {
                        Text("확인")
                    }
                }
            )
        }
    }
}

@Composable
fun PlanInputSection(
    date: String,
    onDateChange: (String) -> Unit,
    target: String,
    onTargetChange: (String) -> Unit,
    site: String,
    onSiteChange: (String) -> Unit,
    memo: String,
    onMemoChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        DateTimeSelection(
            label = "관측 일자",
            datetime = date,
            onPicked = onDateChange
        )

        Spacer(Modifier.height(6.dp))

        Label(
            title = "관측 대상",
            value = target,
            onValueChange = onTargetChange,
            placeholder = "관측 대상을 선택해주세요",
            selectable = false,
            enabled = true
        )

        // 관측지 (선택형)
        Label(
            title = "관측지",
            value = site,
            onValueChange = onSiteChange,
            placeholder = "관측지를 선택해주세요",
            selectable = false,
            enabled = true
        )

        Spacer(Modifier.height(6.dp))
        Divider(color = Color.White.copy(alpha = 0.6f), thickness = 2.dp, modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
    }
}

//@Preview(name = "PlanWriteForm Preview", showBackground = true, backgroundColor = 0xFF241860)
//@Composable
//private fun Preview_PlanWriteForm() { PlanWriteForm(onBack = {}) }