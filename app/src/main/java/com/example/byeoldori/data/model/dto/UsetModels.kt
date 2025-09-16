package com.example.byeoldori.data.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Long? = null,
    val name: String? = null,
    val email: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class UserProfile(
    val nickname: String?,
    val birthdate: String? // yyyy-MM-dd
)
