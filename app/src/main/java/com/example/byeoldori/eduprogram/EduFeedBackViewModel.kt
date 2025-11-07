package com.example.byeoldori.eduprogram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.byeoldori.data.model.dto.FeedbackRequest
import com.example.byeoldori.data.repository.EduRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val repository: EduRepository
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

    // ✅ 어떤 교육 프로그램에 대한 피드백인지 식별용
    private var programId: String = "default_program"   // 실제 값은 화면에서 setProgramId() 호출로 설정

    fun updateProgramId(id: String) {
        programId = id
    }

    /**
     * ✅ 피드백 제출 처리
     * - Repository를 통해 서버에 Feedback POST
     * - 성공/실패 Live2D 반응
     * - 완료 후 onFinished 콜백
     */
    fun submitFeedback() = viewModelScope.launch {

        try {
            // ✅ 요청 모델 생성
            val request = FeedbackRequest(
                programId = programId,
                rating = rating,
                good = goodText,
                bad = badText
            )

            // ✅ 서버 전송 시도
            repository.submitFeedback(request)
        } catch (_: Exception) {

        }
    }

    // ✅ 업데이트 함수들
    fun updateRating(value: Int) { rating = value }
    fun updateGood(value: String) { goodText = value }
    fun updateBad(value: String) { badText = value }
}
