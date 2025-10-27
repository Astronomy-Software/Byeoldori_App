package com.example.byeoldori.ui.components.community

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.relocation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.community.review.*
import com.example.byeoldori.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

// bytes → "123 KB" / "1.2 MB" 포맷
private fun formatSize(bytes: Long?): String {
    if (bytes == null || bytes < 0) return "—"   // ← "오류" 대신 미상 표시
    val kb = (bytes + 1023) / 1024
    return if (kb < 1024) "$kb KB" else String.format(Locale.getDefault(), "%.1f MB", kb / 1024.0)
}

@Composable
fun ContentInput(
    items: List<EditorItem>,
    onItemsChange: (List<EditorItem>) -> Unit,
    uploadItems: List<UploadItem> = emptyList(),
    onPickImages: (onPicked: (List<Uri>) -> Unit) -> Unit,
    onCheck: () -> Unit = {},
    onChecklist: () -> Unit = {},
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    var focusedParagraphIndex by remember { mutableStateOf<Int?>(null) }
    var previewIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {

        // --- 상단 도구 버튼 (작성 모드에서만)
        if (!readOnly) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
            ) {
                IconButton(onClick = { onPickImages { _ -> } }) {
                    Icon(painterResource(R.drawable.ic_photo), contentDescription = "이미지", tint = Color.Unspecified)
                }
                IconButton(onClick = onCheck) {
                    Icon(painterResource(R.drawable.ic_check), contentDescription = "체크", tint = Color.Unspecified)
                }
                IconButton(onClick = onChecklist) {
                    Icon(painterResource(R.drawable.ic_checklist), contentDescription = "체크리스트", tint = Color.Unspecified)
                }
            }
        }

        val paragraphs = items.filterIsInstance<EditorItem.Paragraph>()
        val photos = items.filterIsInstance<EditorItem.Photo>()

        // --- 본문(텍스트)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            paragraphs.forEachIndexed { index, p ->
                if (readOnly) {
                    Text(text = p.value.text, color = TextHighlight)
                } else {
                    val bringIntoViewRequester = remember { BringIntoViewRequester() }
                    val coroutineScope = rememberCoroutineScope()
                    BasicTextField(
                        value = p.value,
                        onValueChange = { new ->
                            val idxInItems = items.indexOfFirst { it is EditorItem.Paragraph && it.id == p.id }
                            if (idxInItems >= 0) {
                                val m = items.toMutableList()
                                val old = m[idxInItems] as EditorItem.Paragraph
                                m[idxInItems] = old.copy(value = new)
                                onItemsChange(m)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusEvent { f ->
                                if (f.isFocused) {
                                    focusedParagraphIndex = index
                                    coroutineScope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            }
                            .defaultMinSize(minHeight = 20.dp)
                            .padding(vertical = 8.dp),
                        textStyle = LocalTextStyle.current.copy(color = TextHighlight),
                        cursorBrush = SolidColor(TextDisabled),
                        maxLines = Int.MAX_VALUE,
                        decorationBox = { inner ->
                            if (p.value.text.isEmpty()) {
                                Text("내용을 입력해주세요", color = TextDisabled.copy(alpha = 0.4f))
                            }
                            inner()
                        }
                    )
                }
            }
        }

        //업로드된 파일명 리스트
        if (!readOnly && uploadItems.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                uploadItems.forEach { item ->
                    val color = when (item.status) {
                        UploadStatus.UPLOADING -> WarningYellow
                        UploadStatus.DONE -> SuccessGreen
                        UploadStatus.ERROR -> ErrorRed
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sizeText = formatSize(item.sizeBytes)
                        val textToShow = if (sizeText == "—") item.name else "${item.name}  ($sizeText)"
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = textToShow,
                                color = TextHighlight,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                            if (item.status == UploadStatus.UPLOADING) {
                                Spacer(Modifier.width(8.dp))
                                LinearProgressIndicator(
                                    progress = item.progress,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(100)),
                                    color = Purple500,
                                    trackColor = Color.Transparent
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${(item.progress * 100).toInt()}%",
                                    color = TextDisabled,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Text(
                            text = when (item.status) {
                                UploadStatus.UPLOADING -> "업로드 중..."
                                UploadStatus.DONE -> "완료"
                                UploadStatus.ERROR -> "실패"
                            },
                            color = color,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        //하단 사진 LazyRow 갤러리
        if (photos.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            LazyRow( //수평 스크롤 리스트
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(photos.size) { i ->
                    val photo = photos[i]
                    val context = LocalContext.current
                    val imageLoader = context.imageLoader //Coil이 이미지를 로드할 때 사용할 ImageLoader를 가져옴

                    AsyncImage(
                        model = photo.model,
                        imageLoader = imageLoader,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(250.dp)
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { previewIndex = i }
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
        previewIndex?.let { start ->
            FullScreenImage(
                models = photos.map { it.model },
                startIndex = start,
                onDismiss = { previewIndex = null }
            )
        }
    }
}

@Preview(showBackground = true, name = "ContentInput - Write Mode")
@Composable
fun Preview_ContentInput_WriteMode() {
    MaterialTheme {
        Surface(color = Color(0xFF241860)) {
            var items = emptyList<EditorItem>()

            // 업로드 상태 샘플
            val uploadItems = listOf(
                UploadItem(name = "orion_1.jpg", status = UploadStatus.UPLOADING,sizeBytes = 128_500, progress = 0.42f),
                UploadItem(name = "orion_2.jpg", status = UploadStatus.DONE, url = "https://picsum.photos/400/300",sizeBytes = 1_734_003, progress = 1f ),
                UploadItem(name = "orion_3.jpg", status = UploadStatus.ERROR,sizeBytes = null, progress = 0f),
            )

            ContentInput(
                items = items,
                onItemsChange = { items = it },
                uploadItems = uploadItems,
                onPickImages = {},
                onCheck = { /* no-op for preview */ },
                onChecklist = { /* no-op for preview */ },
                readOnly = false,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}