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
data class UpdateUserProfile(
    val nickname: String?,
    val birthdate: String? // yyyy-MM-dd
)


@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: Long,
    val email: String,
    val name: String,
    val phone: String?,
    val nickname: String?,
    val birthdate: String?, // yyyy-MM-dd
    val emailVerified: Boolean,
    val lastLoginAt: String?,
    val roles: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val passwordHash: String? = null,
    val onboardingRequired: Boolean? = null,
    // passwordHash 는 보안상 굳이 앱에서 쓸 필요 없으면 뺄 수 있음
)