package com.example.byeoldori.data.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FileUploadResponse(
    val success: Boolean,
    val message: String,
    val data: FileDto
)

@JsonClass(generateAdapter = true)
data class FileDto(
    val url: String,
    val filename: String,
    val size: Long,
    val contentType: String
)