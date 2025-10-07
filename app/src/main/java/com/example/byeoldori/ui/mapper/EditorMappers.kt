package com.example.byeoldori.ui.mapper

import androidx.compose.ui.text.input.TextFieldValue
import com.example.byeoldori.domain.Content
import com.example.byeoldori.ui.components.community.EditorItem

fun List<EditorItem>.toDomain(): List<Content> =
    mapNotNull {
        when (it) {
            is EditorItem.Paragraph -> Content.Text(it.value.text)
            is EditorItem.Photo -> when (val m = it.model) {
                is android.net.Uri -> Content.Image.Url(m.toString())
                is String -> Content.Image.Url(m)
                is Int -> Content.Image.Resource(m)
                else -> null
            }
        }
    }

fun List<Content>.toUi(): List<EditorItem> =
    map {
        when (it) {
            is Content.Text -> EditorItem.Paragraph(value = TextFieldValue(it.text))
            is Content.Image.Url -> EditorItem.Photo(model = it.url)
            is Content.Image.Resource -> EditorItem.Photo(model = it.resId)
        }
    }
