package com.example.byeoldori

// src/test/java/com/example/byeoldori/data/api/ApiServiceTest.kt
import com.example.byeoldori.data.api.ApiService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ApiServiceTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    // Hilt가 NetworkModule에서 제공하는 실제 ApiService를 주입합니다.
    @Inject
    lateinit var apiService: ApiService

    // Hilt 테스트 환경을 초기화합니다.
    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun login_shouldReturnLoginResponse() = runBlocking {
        // 실제 통신을 위한 유효한 이메일과 비밀번호를 사용하세요.
        // 테스트 서버가 필요하며, 이 값은 유출되면 안 됩니다.
        val email = "test@example.com"
        val password = "password123"

        // 실제 API 엔드포인트에 요청을 보냅니다.
        val loginResponse = apiService.login(LoginRequest(email, password))

        // 응답이 null이 아니고, 유효한 토큰이 포함되어 있는지 확인합니다.
        assertNotNull(loginResponse)
        assertNotNull(loginResponse.bearer)
    }

    @Test
    fun getMe_shouldReturnUserDto() = runBlocking {
        // 이 테스트는 `AuthInterceptor`가 토큰을 성공적으로 추가한다는 전제하에 작동합니다.
        // 따라서 먼저 `login` 테스트를 실행하여 토큰을 DataStore에 저장해야 합니다.

        // 실제 API 요청을 보냅니다.
        val userDto = apiService.getMe()

        // 응답이 null이 아니고, 유효한 사용자 ID가 포함되어 있는지 확인합니다.
        assertNotNull(userDto)
        assertNotNull(userDto.id)
    }
}