package com.example.byeoldori.eduprogram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.FeedbackRequest
import com.example.byeoldori.data.repository.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ===============================================================
 * ✅ EduFeedbackViewModel (Repository 연동 버전)
 * ---------------------------------------------------------------
 * 역할:
 * - 별점 / 좋았던 점 / 아쉬웠던 점 상태 저장
 * - Repository 호출하여 피드백 서버 전송
 * - Live2D 반응 + 후처리(onFinished)
 * ===============================================================
 */

@HiltViewModel
class EduFeedbackViewModel @Inject constructor(
    private val repository: EducationRepository
) : ViewModel() {
    private val _goFeedback = MutableStateFlow(false)
    val goFeedback = _goFeedback

    fun requestFeedback() {
        _goFeedback.value = true
    }

    fun consumeFeedbackNavigation() {
        _goFeedback.value = false
    }

    // ✅ 입력 값 상태
    var rating: Int = 0
    var goodText: String = ""
    var badText: String = ""

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun consumeToastMessage() {
        _toastMessage.value = null
    }

    // ✅ 어떤 교육 프로그램에 대한 피드백인지 식별용
    private var postId: Long = 0    // 실제 값은 화면에서 setProgramId() 호출로 설정

    fun updatepostId(id: Long) {
        postId = id
    }

    /**
     * ✅ 피드백 제출 처리
     * - Repository를 통해 서버에 Feedback POST
     * - 성공/실패 Live2D 반응
     * - 완료 후 onFinished 콜백
     */
    fun submitFeedback() = viewModelScope.launch {
        try {
            val request = FeedbackRequest(
                score = rating,
                pros = goodText,
                cons = badText
            )

            val res = repository.submitFeedback(postId = postId, request)
            println("✅ 피드백 전송 성공: $res")

            _toastMessage.value = "평가해주셔서 감사합니다!"
            delay(500)
            _goFeedback.value = false

        } catch (e: Exception) {
            println("❌ 피드백 전송 실패 : ${e.message}")
            _toastMessage.value = "피드백 전송 실패 : ${e.message}"
        }
    }

    // ✅ 업데이트 함수들
    fun updateRating(value: Int) { rating = value }
    fun updateGood(value: String) { goodText = value }
    fun updateBad(value: String) { badText = value }
}
