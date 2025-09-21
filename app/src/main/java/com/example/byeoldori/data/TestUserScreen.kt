package com.example.byeoldori.data

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.local.datastore.TokenDataStore
import com.example.byeoldori.data.model.common.ApiResponse
import com.example.byeoldori.data.model.dto.UpdateUserProfile
import com.example.byeoldori.data.model.dto.UserProfile
import com.example.byeoldori.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI 상태 정의
sealed class UserUiState {
    object Idle : UserUiState()
    object Loading : UserUiState()
    data class Success<T>(val data: T) : UserUiState()
    data class Error(val message: String) : UserUiState()
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repo: UserRepository,
    private val tokenStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val uiState: StateFlow<UserUiState> = _uiState

    fun getMyProfile() = viewModelScope.launch {
        _uiState.value = UserUiState.Loading
        try {
            val res: ApiResponse<UserProfile> = repo.getMyProfile()
            if (res.success) {
                _uiState.value = UserUiState.Success(res.data)
            } else {
                _uiState.value = UserUiState.Error(res.message)
            }
        } catch (e: Exception) {
            _uiState.value = UserUiState.Error(e.message ?: "알 수 없는 오류")
        }
    }

    fun updateMe(profile: UpdateUserProfile) = viewModelScope.launch {
        _uiState.value = UserUiState.Loading
        try {
            val res = repo.updateMe(profile)
            if (res.success) {
                _uiState.value = UserUiState.Success("프로필 수정 성공")
            } else {
                _uiState.value = UserUiState.Error(res.message)
            }
        } catch (e: Exception) {
            _uiState.value = UserUiState.Error(e.message ?: "알 수 없는 오류")
        }
    }

    fun resign() = viewModelScope.launch {
        _uiState.value = UserUiState.Loading
        try {
            val res = repo.resign()
            if (res.success) {
                _uiState.value = UserUiState.Success("회원 탈퇴 완료")
            } else {
                _uiState.value = UserUiState.Error(res.message)
            }
        } catch (e: Exception) {
            _uiState.value = UserUiState.Error(e.message ?: "알 수 없는 오류")
        }
    }

    fun logOut() = viewModelScope.launch {
        try {
            val res = repo.logOut()
            tokenStore.clear()
            _uiState.value = UserUiState.Success("로그아웃 완료")
        } catch (e: Exception) {
            tokenStore.clear()
            _uiState.value = UserUiState.Error(e.message ?: "로그아웃 실패")
        }
    }
}

@Composable
fun TestUserScreen(
    vm: UserViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("👤 사용자 테스트 UI", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        when (state) {
            is UserUiState.Idle -> Text("대기 중…")
            is UserUiState.Loading -> CircularProgressIndicator()
            is UserUiState.Error -> Text(
                "에러: ${(state as UserUiState.Error).message}",
                color = MaterialTheme.colorScheme.error
            )
            is UserUiState.Success<*> -> {
                val data = (state as UserUiState.Success<*>).data
                when (data) {
                    is UserProfile -> {
                        Text("닉네임: ${data.nickname ?: "-"}")
                        Text("생일: ${data.birthdate ?: "-"}")
                    }
                    else -> Text(data.toString())
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 버튼들
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { vm.getMyProfile() }) { Text("내 프로필") }
            Button(onClick = { vm.logOut() }) { Text("로그아웃") }
        }

        Spacer(Modifier.height(12.dp))

        // 프로필 수정용 입력창
        var nickname by remember { mutableStateOf("") }
        var birthdate by remember { mutableStateOf("") }

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("닉네임") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = birthdate,
            onValueChange = { birthdate = it },
            label = { Text("생년월일 (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                vm.updateMe(UpdateUserProfile(nickname.ifBlank { null }, birthdate.ifBlank { null }))
                nickname = ""; birthdate = ""
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("프로필 수정")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { vm.resign() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("회원 탈퇴", color = MaterialTheme.colorScheme.onError)
        }
    }
}
