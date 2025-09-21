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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.viewmodel.Observatory.Review
import java.util.UUID

@Composable
fun ReviewWriteForm(
    author: String,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,           // 등록
    onTempSave: () -> Unit,         // 임시 저장
    onMore: () -> Unit,
    now: () -> Long = { System.currentTimeMillis() },
    onSubmitReview: (Review) -> Unit,
    initialReview: Review? = null
) {
    // --- 상태들 ---
    var showCancelDialog by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableStateOf("") }
    var ratingInt by rememberSaveable { mutableStateOf(0) }
    var siteScore by rememberSaveable { mutableStateOf("") }
    var siteScoreInt by rememberSaveable { mutableStateOf(0) }
    var target by rememberSaveable { mutableStateOf("") }
    var site by rememberSaveable { mutableStateOf("") }
    var equipment by rememberSaveable { mutableStateOf("") }
    var startTime by rememberSaveable { mutableStateOf("") }
    var endTime by rememberSaveable { mutableStateOf("") }
    var showRatingPicker by remember { mutableStateOf(false) }
    var showSiteScorePicker by remember { mutableStateOf(false) }

    var pendingOnPicked by remember { mutableStateOf<((List<Uri>) -> Unit)?>(null) }

    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris -> //사진 선택 끝나면 전달받은 uri 리스트
        // ContentInput이 넘겨준 onPicked를 여기서 호출
        pendingOnPicked?.invoke(uris ?: emptyList())
        pendingOnPicked = null
    }

    var items by remember {
        mutableStateOf(listOf<EditorItem>(EditorItem.Paragraph())) //리뷰 본문
    }
    var date by rememberSaveable { mutableStateOf(initialReview?.date ?: "") }

    fun makeReview(): Review {
        val createdAt = now()
        return Review(
            id = UUID.randomUUID().toString(),
            title = title,
            author = author,
            rating = ratingInt,
            likeCount = 0,
            commentCount = 0,
            profile = R.drawable.profile1,
            viewCount = 0,
            createdAt = createdAt,
            target = target,
            site = site,
            date = date,
            siteScore = siteScoreInt,
            equipment = equipment,
            startTime = startTime,
            endTime = endTime,
            contentItems = items
        )
    }

    fun validateRequirement(): Boolean {
        return title.isNotBlank() &&
                target.isNotBlank() &&
                site.isNotBlank() &&
                equipment.isNotBlank() &&
                date.isNotBlank() &&
                ratingInt > 0 &&
                siteScoreInt > 0
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
                            onSubmitReview(makeReview()) //입력된 값을 모아서 Review객체 생성
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
                ReviewInput(
                    target = target,
                    onTargetChange = { target = it },
                    site = site,
                    onSiteChange = { site = it },
                    equipment = equipment,
                    onEquipmentChange = { equipment = it },
                    startTime = startTime,
                    endTime = endTime,
                    onTimeChange = { s, e -> startTime = s; endTime = e },
                    rating = rating,
                    onRatingChange = { showRatingPicker = true },
                    siteScore = siteScore,
                    onSiteScoreChange = { showSiteScorePicker = true },
                    modifier = Modifier.padding(vertical = 5.dp),
                    date = date,
                    onDateChange = { picked -> date = picked }
                ) }
            item {
                ContentInput(
                    items = items,
                    onItemsChange = { items = it },
                    onPickImages = { onPicked ->
                        // ContentInput이 넘겨준 onPicked를 보관했다가,
                        pendingOnPicked = onPicked
                        // 시스템 픽커 실행(선택된 이미지 uri리스트를 받아 item에 반영)
                        pickImages.launch( //갤러리 실행 런처를 실행
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSubmit = {
                        onSubmitReview(makeReview())
                        onSubmit()
                    },
                    onChecklist = {
                        // TODO: 체크리스트 열기 등 원하는 동작
                    }
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
        if (showSiteScorePicker) { //관측지 점수 선택
            ScoreLabel(
                show = showSiteScorePicker,
                score = siteScoreInt,
                onSelected = { n ->
                    siteScoreInt = n
                    siteScore = "$n/5"
                    showSiteScorePicker = false
                },
                onDismiss = { showSiteScorePicker = false }
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
            onMore = {},
            onSubmitReview = {}
        )
    }
}