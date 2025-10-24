package com.example.byeoldori.ui.components.community.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.common.copyUriToCache
import com.example.byeoldori.data.model.dto.ObservationSite
import com.example.byeoldori.domain.Content
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.mapper.toUi
import com.example.byeoldori.viewmodel.Community.FileUploadViewModel
import com.example.byeoldori.viewmodel.Community.ReviewViewModel
import com.example.byeoldori.viewmodel.Observatory.ObservatoryMapViewModel
import com.example.byeoldori.viewmodel.UiState
import kotlinx.coroutines.launch

enum class UploadStatus { UPLOADING, DONE, ERROR }

data class UploadItem(
    val name: String,          // 예: 이미지1.png
    val url: String? = null,   // 업로드 완료시 서버 URL
    val status: UploadStatus = UploadStatus.UPLOADING,
    val sizeBytes: Long? = null,
    val progress: Float = 0f   // 진행률(프로그래스바)
)

private fun findMatchingSiteId(siteName: String, allSites: List<ObservationSite>): Long? {
    val normalizedInput = siteName.trim().replace(" ", "")
    return allSites.firstOrNull { site ->
        val normalizedSiteName = site.name.trim().replace(" ", "")
        normalizedSiteName.contains(normalizedInput, ignoreCase = true)
    }?.id
}

@Composable
fun ReviewWriteForm(
    author: String,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,           // 등록
    onTempSave: () -> Unit,         // 임시 저장
    onMore: () -> Unit,
    now: () -> Long = { System.currentTimeMillis() },
    vm: ReviewViewModel? = null,
    initialReview: Review? = null,
    observatoryVm: ObservatoryMapViewModel = hiltViewModel()
) {
    // --- 상태들 ---
    val isEditMode = initialReview != null
    var showCancelDialog by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf( initialReview?.title ?: "") }
    var ratingInt by rememberSaveable { mutableStateOf(initialReview?.rating ?: 0) }
    var rating by rememberSaveable { mutableStateOf(if (ratingInt > 0) "$ratingInt/5" else "") }
    var target by rememberSaveable { mutableStateOf(initialReview?.target ?: "") }
    var site by rememberSaveable { mutableStateOf(initialReview?.site ?: "") }
    var equipment by rememberSaveable { mutableStateOf(initialReview?.equipment ?: "") }
    var date by rememberSaveable { mutableStateOf(initialReview?.date ?: "") }
    var startTime by rememberSaveable { mutableStateOf("") }
    var endTime by rememberSaveable { mutableStateOf("") }
    var showRatingPicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fileUploadVm: FileUploadViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        fileUploadVm.reset() // 이전 Success/Error 잔상 제거
    }
    val siteState by observatoryVm.state.collectAsState()
    val sites = when (val s = siteState) {
        is UiState.Success -> s.data       // 관측지 리스트
        else -> emptyList()
    }
    LaunchedEffect(Unit) {
        observatoryVm.loadSites()  // 관측지 리스트 불러오기
    }

    val uploadState by fileUploadVm.uploadState.collectAsState()
    var items by remember(initialReview?.id) {
        mutableStateOf(
            initialReview?.contentItems?.toUi() ?: emptyList()
        )
    }
    LaunchedEffect(initialReview?.id) {
        if (items.none { it is EditorItem.Paragraph }) {
            items = items + EditorItem.Paragraph()
        }
    }
    //갤러리 선택 후 삽입 콜백을 잠시 보관
    var pendingOnPicked by remember { mutableStateOf<((List<Uri>) -> Unit)?>(null) }
    var uploadItems by remember(initialReview?.id) {
        mutableStateOf(
            if (isEditMode)
                initialReview!!.contentItems
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
    } //현재 업로드 중인 파일 목록을 상태로 관리

    //갤러리 UI 열어주고 선택된 이미지들의 Uri리스트를 콜백으로 돌려줌
    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris -> //uri리스트
        val picked = uris ?: emptyList() //아무것도 안골랐을 수도 있으니 nullable

        // ContentInput이 넘겨준 onPicked를 여기서 호출
        pendingOnPicked?.invoke(picked)
        pendingOnPicked = null

        picked.forEach { uri ->
            scope.launch {//선택한 각 Uri를 순회하면서 코루틴으로 비동기 처리
                val file = copyUriToCache(context, uri) //URI → File 변환 함수
                fileUploadVm.reset()

                val afdLen = context.contentResolver //uRI가 가리키는 파일의 크기를 알아냄
                    .openAssetFileDescriptor(uri,"r") //시스템이 파일을 대신 열어줌
                    ?.length
                    ?: -1L
                val size = if(afdLen > 0) afdLen else file.length()

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
    val createStateState = vm?.createState?.collectAsState()
    val createState = createStateState?.value ?: UiState.Idle

    //서버 업로드가 끝나서 성공적으로 URL을 받은 이미지들만 저장하는 리스트
    var uploadedImageUrls by remember {
        mutableStateOf(
            initialReview?.contentItems //기존 이미지 있으면 미리 URL로 채워두기
                ?.filterIsInstance<Content.Image.Url>()
                ?.map { it.url }
                ?: emptyList()
        )
    }

    fun buildContentText(items: List<EditorItem>): String =
        items.joinToString("\n") {
            when (it) {
                is EditorItem.Paragraph -> it.value.text
                else -> ""
            }
        }

    fun validateRequirement(): Boolean {
        return title.isNotBlank() &&
                target.isNotBlank() &&
                site.isNotBlank() &&
                equipment.isNotBlank() &&
                date.isNotBlank() &&
                ratingInt > 0
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
                items = items + EditorItem.Photo(model = url)

                uploadedImageUrls = uploadedImageUrls + url
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
                fileUploadVm.reset()
            }
            else -> Unit
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .offset(y = 10.dp)
                .advancedImePadding() //중요
                //.windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom))
        ) {
            item {
                WriteBar(
                    onSubmit = {
                        if(validateRequirement()) {
                            val matchedId = findMatchingSiteId(site, sites)
                            val payloadContent = buildContentText(items)

                            if (isEditMode) {
                                val idL = initialReview!!.id.toLong()
                                vm?.updateReview(
                                    postId = idL,
                                    title = title.trim(),
                                    content = payloadContent,
                                    location = site.trim(),
                                    target = target.trim(),
                                    equipment = equipment.trim(),
                                    observationDate = date,
                                    score = ratingInt,
                                    observationSiteId = matchedId,
                                    imageUrls = uploadedImageUrls
                                ) {
                                    onSubmit() // 상위 완료 처리(알림, 다이얼로그 등)
                                }
                            } else {
                                vm?.createReview(
                                    title = title.trim(),
                                    content = buildContentText(items),
                                    location = site.trim(),
                                    target = target.trim(),
                                    equipment = equipment.trim(),
                                    observationDate = date,
                                    score = ratingInt,
                                    imageUrls = uploadedImageUrls,
                                    observationSiteId = matchedId
                                )
                            }
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
                ReviewInput(
                    target = target,
                    onTargetChange = { target = it },
                    site = site,
                    onSiteChange = { site = it },
                    equipment = equipment,
                    onEquipmentChange = { equipment = it },
                    onTimeChange = { s, e -> startTime = s; endTime = e },
                    rating = rating,
                    onRatingChange = { showRatingPicker = true },
                    modifier = Modifier.padding(vertical = 5.dp),
                    date = date,
                    onDateChange = { picked -> date = picked }
                ) }
            item {
                ContentInput(
                    items = items,
                    onItemsChange = { newItems -> items = newItems },
                    uploadItems = uploadItems,
                    onPickImages = { onPicked ->
                        // ContentInput이 넘겨준 onPicked를 보관했다가,
                        pendingOnPicked = onPicked
                        // 시스템 픽커 실행(선택된 이미지 uri리스트를 받아 item에 반영)
                        pickImages.launch( //갤러리 실행 런처를 실행
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onCheck = {},
                    onChecklist = {
                        // TODO: 체크리스트 열기 등 원하는 동작
                    }
                )
            }
        }

        if(!isEditMode) {
            when (val s = createState) {
                is UiState.Loading -> { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                is UiState.Success -> {
                    LaunchedEffect(s) {
                        val first = uploadedImageUrls.firstOrNull()
                        val createdId: Long = s.data
                        vm?.registerLocalThumbnail(createdId.toString(), first)
                        onSubmit()
                    }
                }
                is UiState.Error -> { Text(s.message ?: "작성 실패", color = Color.Red) }
                UiState.Idle -> Unit
            }
        }

        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                text = { Text("작성 중인 내용을 취소하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        showCancelDialog = false
                        onCancel()
                    }) { Text("예") }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) { Text("아니오") }
                }
            )
        }
        if (showRatingPicker) {
            ScoreLabel(
                show = showRatingPicker,
                score = ratingInt,
                onSelected = { n -> //별점 선택
                    ratingInt = n
                    rating = "$n/5"
                    showRatingPicker = false
                },
                onDismiss = { showRatingPicker = false }
            )
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
}

@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 360, heightDp = 720)
@Composable
private fun Preview_ReviewWriteForm_Box() {
    MaterialTheme {
        ReviewWriteForm(
            author = "astro_user",
            onCancel = {},
            onSubmit = {},
            onTempSave = {},
            onMore = {}
        )
    }
}