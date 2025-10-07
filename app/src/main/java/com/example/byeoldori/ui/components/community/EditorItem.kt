package com.example.byeoldori.ui.components.community

import androidx.compose.ui.text.input.TextFieldValue

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
