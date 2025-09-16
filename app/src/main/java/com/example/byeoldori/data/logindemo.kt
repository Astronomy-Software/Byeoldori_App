package com.example.byeoldori.data

// 로그인 데모용으로 만든친구인데 API 구조 참조할때 확인하세요

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.BuildConfig
import com.example.byeoldori.data.model.common.TokenData
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// ──────────────────────────────────────────────────────────────────────────────
// 2) DTO (요청/응답 데이터 모델)  ← 코드젠 필수: @JsonClass(generateAdapter = true)
// ──────────────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: TokenData
)

// ──────────────────────────────────────────────────────────────────────────────
// 3) Retrofit API
// ──────────────────────────────────────────────────────────────────────────────
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
}

// ──────────────────────────────────────────────────────────────────────────────
// 4) 간단한 토큰 저장소 (데모)
// ──────────────────────────────────────────────────────────────────────────────
object InMemoryTokenStore {
    var accessToken: String? = null
    var refreshToken: String? = null
    fun clear() { accessToken = null; refreshToken = null }
}

// ──────────────────────────────────────────────────────────────────────────────
// 5) OkHttp Interceptor
// ──────────────────────────────────────────────────────────────────────────────
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = InMemoryTokenStore.accessToken
        val req = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else original
        return chain.proceed(req)
    }
}

// ──────────────────────────────────────────────────────────────────────────────
object Network {
    // 반드시 슬래시로 끝나야 함
    private const val BASE_URL = BuildConfig.BASE_URL

    // ✅ 코드젠이 어댑터를 생성하므로 Moshi는 “순정”으로 생성
    private val moshi: Moshi by lazy { Moshi.Builder().build() }

    private val logging: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
}

// ──────────────────────────────────────────────────────────────────────────────
class AuthRepository(private val api: AuthApi) {
    suspend fun login(email: String, password: String): TokenData {
        val resp = api.login(LoginRequest(email, password))
        val tokenData = resp.data
        InMemoryTokenStore.accessToken = tokenData.accessToken
        InMemoryTokenStore.refreshToken = tokenData.refreshToken
        return tokenData
    }
}

// ──────────────────────────────────────────────────────────────────────────────
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val tokens: TokenData) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

// ──────────────────────────────────────────────────────────────────────────────
class LoginViewModel(
    application: Application,
    private val repo: AuthRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val tokens = repo.login(email, password)
                _uiState.value = LoginUiState.Success(tokens)
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    fun logout() {
        InMemoryTokenStore.clear()
        _uiState.value = LoginUiState.Idle
    }
}

// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(vm: LoginViewModel) {
    val uiState by vm.uiState.collectAsState()
    val focus = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("로그인", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focus.clearFocus()
                vm.login(email.trim(), password)
            }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                focus.clearFocus()
                vm.login(email.trim(), password)
            },
            enabled = uiState !is LoginUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState is LoginUiState.Loading) "로그인 중..." else "로그인")
        }

        Spacer(Modifier.height(12.dp))

        when (uiState) {
            is LoginUiState.Idle -> Text("이메일과 비밀번호를 입력하세요.")
            is LoginUiState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
            is LoginUiState.Success -> {
                val s = uiState as LoginUiState.Success
                Text("로그인 성공!")
                Spacer(Modifier.height(8.dp))
                Text("AccessToken: ${s.tokens.accessToken.take(12)}…")
                Text("만료(AT): ${s.tokens.accessTokenExpiresAt}")
                Text("만료(RT): ${s.tokens.refreshTokenExpiresAt}")
            }

            is LoginUiState.Error -> {
                val e = uiState as LoginUiState.Error
                Text("오류: ${e.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
