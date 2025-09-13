package com.example.byeoldori.data.repository

import com.example.byeoldori.data.model.dto.UserDto

interface UserRepository {
    suspend fun getMe(): Result<UserDto>
    suspend fun getUser(id: Long): Result<UserDto>
}
