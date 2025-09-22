package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.AuthApi
import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.dto.FindEmailRequset
import com.example.byeoldori.data.model.dto.ResetPasswordToEmailRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindAccountRepository @Inject constructor(
    private val authApi: AuthApi
) {
    // TODO : ResetPasswordToEmail 이친구 보안성 약해서 수정해야함.
    suspend fun findEmail(req: FindEmailRequset): ApiResponse<String> {
        return authApi.findEmail(req)
    }

    suspend fun resetPasswordToEmail(req: ResetPasswordToEmailRequest): ApiResponse<String> {
        return authApi.resetPasswordToEmail(req)
    }
}
