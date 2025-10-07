package com.example.byeoldori.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.byeoldori.R


sealed class Content {
    data class Text(val text: String) : Content()
    sealed class Image : Content() {
        data class Url(val url: String) : Image()
        data class Resource(val resId: Int) : Image()
    }
}