package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.ApiService
import com.example.byeoldori.data.model.dto.UserDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: ApiService
) : UserRepository {

    override suspend fun getMe(): Result<UserDto> = runCatching { api.getMe() }

    override suspend fun getUser(id: Long): Result<UserDto> = runCatching { api.getUser(id) }
}
