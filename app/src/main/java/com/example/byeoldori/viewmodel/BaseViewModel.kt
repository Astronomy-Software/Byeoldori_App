package com.example.byeoldori.viewmodel

import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    protected fun handleException(e: Exception): String = when (e) {
        is retrofit2.HttpException -> parseHttpError(e)
        is java.net.UnknownHostException -> "네트워크 연결을 확인하세요."
        else -> "알 수 없는 오류: ${e.message}"
    }

    private fun parseHttpError(e: retrofit2.HttpException): String {
        val errorBody = e.response()?.errorBody()?.string()
        val serverMsg = errorBody?.let { body ->
            Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
                .find(body)?.groupValues?.getOrNull(1)
        }
        // 👇 상태 코드 포함
        return "[${e.code()}] ${serverMsg ?: "서버 오류"}"
    }
}