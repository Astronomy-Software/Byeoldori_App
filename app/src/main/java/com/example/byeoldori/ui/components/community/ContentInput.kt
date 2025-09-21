package com.example.byeoldori.ui.components.community

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import kotlinx.coroutines.launch

sealed class EditorItem(open val id: String) {
    data class Paragraph(
        override val id: String = java.util.UUID.randomUUID().toString(),
        val value: TextFieldValue = TextFieldValue("")
    ) : EditorItem(id)

    data class Photo(
        override val id: String = java.util.UUID.randomUUID().toString(),
        val model: Any
    ) : EditorItem(id)
}

@Composable
fun ContentInput(
    items: List<EditorItem>,
    onItemsChange: (List<EditorItem>) -> Unit,
    onSubmit: () -> Unit = {},
    onPickImages: (onPicked: (List<Uri>) -> Unit) -> Unit,
    onChecklist: () -> Unit = {},
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    var focusedParagraphIndex by remember { mutableStateOf<Int?>(null) }

    // 커서 위치에 사진 삽입
    val insertAtCaret: (List<Uri>) -> Unit = caret@ { uris ->
        if (uris.isEmpty()) return@caret
        val idx = focusedParagraphIndex ?: items.indexOfFirst { it is EditorItem.Paragraph }
        if (idx == -1) return@caret

        val p = items[idx] as EditorItem.Paragraph
        val caret = p.value.selection.start.coerceIn(0, p.value.text.length)
        val before = p.value.text.substring(0, caret)
        val after  = p.value.text.substring(caret)

        val newList = buildList {
            addAll(items.take(idx))
            add(EditorItem.Paragraph(value = TextFieldValue(before)))
            uris.forEach { add(EditorItem.Photo(model = it)) }
            add(EditorItem.Paragraph(value = TextFieldValue(after)))
            addAll(items.drop(idx + 1))
        }
        onItemsChange(newList)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (!readOnly) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
            ) {
                IconButton(onClick = {
                    onPickImages { uris -> insertAtCaret(uris) }
                }) {
                    Icon(painterResource(R.drawable.ic_photo), contentDescription = "이미지", tint = Color.Unspecified)
                }
                IconButton(onClick = onSubmit) {
                    Icon(painterResource(R.drawable.ic_check), contentDescription = "등록", tint = Color.Unspecified)
                }
                IconButton(onClick = onChecklist) {
                    Icon(painterResource(R.drawable.ic_checklist), contentDescription = "체크리스트", tint = Color.Unspecified)
                }
            }
        }

        // 텍스트+이미지 통합 리스트
        Column(
            modifier = Modifier
                .fillMaxWidth(),
                //.imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items.forEachIndexed { index, item ->
                var offsetY by remember(item.id) { mutableStateOf(0f) }
                var heightPx by remember(item.id) { mutableStateOf(0f) }

                val dragModifier = if (readOnly) {
                    Modifier
                } else {
                    Modifier.pointerInput(item.id) {
                        detectDragGesturesAfterLongPress(
                            onDragEnd = { offsetY = 0f }
                        ) { change, drag ->
                            change.consume()
                            offsetY += drag.y
                            val threshold = heightPx * 0.6f
                            val cur = index
                            val target = when {
                                offsetY < -threshold && cur > 0 -> cur - 1
                                offsetY >  threshold && cur < items.lastIndex -> cur + 1
                                else -> cur
                            }
                            if (target != cur) {
                                val m = items.toMutableList()
                                val moved = m.removeAt(cur)
                                m.add(target, moved)
                                onItemsChange(m)
                                offsetY = 0f
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { heightPx = it.height.toFloat() }
                        .then(dragModifier)
                        .graphicsLayer(translationY = offsetY)
                ) {
                    when (item) {
                        is EditorItem.Paragraph -> {
                            if (readOnly) {
                                Text(
                                    text = item.value.text,
                                    color = TextHighlight
                                )
                            } else {
                                // 내용 입력(텍스트)
                                val bringIntoViewRequester = remember { BringIntoViewRequester() }
                                val coroutineScope = rememberCoroutineScope()

                                BasicTextField(
                                    value = item.value,
                                    onValueChange = { new ->
                                        val m = items.toMutableList()
                                        m[index] = EditorItem.Paragraph(id = item.id, value = new)
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
                                        if (item.value.text.isEmpty()) {
                                            Text("내용을 입력해주세요", color = TextDisabled.copy(alpha = 0.4f))
                                        }
                                        inner()
                                    }
                                )
                            }
                        }
                        is EditorItem.Photo -> {
                            Box {
                                if (item.model is Int) {
                                    //프리뷰에서 이미지가 안보여서
                                    Image(
                                        painter = painterResource(item.model as Int),
                                        contentDescription = null,
                                        contentScale = ContentScale.FillWidth,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    AsyncImage(
                                        model = item.model,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .width(200.dp)
                                            .height(300.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }

                                if (!readOnly) {
                                    IconButton(
                                        onClick = {
                                            val m = items.toMutableList()
                                            m.removeAt(index)
                                            onItemsChange(m)
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
