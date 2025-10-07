package com.example.byeoldori.domain

sealed class Content {
    data class Text(val text: String) : Content()
    sealed class Image : Content() {
        data class Url(val url: String) : Image()
        data class Resource(val resId: Int) : Image()
    }
}