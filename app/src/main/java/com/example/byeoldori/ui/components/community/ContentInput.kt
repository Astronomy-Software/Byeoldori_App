package com.example.byeoldori.ui.components.community

import android.net.Uri
import androidx.compose.foundation.background
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
                            val m = items.toMutableList()
                            m[index] = EditorItem.Paragraph(id = p.id, value = new)
                            onItemsChange(m)
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.name,
                            color = TextHighlight,
                            style = MaterialTheme.typography.bodySmall
                        )
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
        if (readOnly && photos.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            LazyRow( //수평 스크롤 리스트
                modifier = Modifier.fillMaxWidth().height(200.dp),
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
                    )
                }
            }
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
                UploadItem(name = "orion_1.jpg", status = UploadStatus.UPLOADING),
                UploadItem(name = "orion_2.jpg", status = UploadStatus.DONE, url = "https://picsum.photos/400/300"),
                UploadItem(name = "orion_3.jpg", status = UploadStatus.ERROR),
            )

            ContentInput(
                items = items,
                onItemsChange = { items = it },
                uploadItems = uploadItems,
                onPickImages = { onPicked ->
                    // 프리뷰에선 실제 갤러리 런처가 없으므로,
                    // 더미 URI 리스트를 넘겨주는 형태로 동작만 시뮬레이션 할 수 있음.
                    onPicked(
                        listOf(
                            Uri.parse("content://dummy/image/1"),
                            Uri.parse("content://dummy/image/2")
                        )
                    )
                },
                onCheck = { /* no-op for preview */ },
                onChecklist = { /* no-op for preview */ },
                readOnly = false,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}