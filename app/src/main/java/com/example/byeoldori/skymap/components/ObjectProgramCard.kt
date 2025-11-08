package com.example.byeoldori.skymap.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.ui.components.community.program.*
import com.example.byeoldori.ui.components.observatory.ReviewSection
import com.example.byeoldori.ui.theme.TextDisabled
import com.example.byeoldori.viewmodel.Community.*
import com.example.byeoldori.viewmodel.UiState

@Composable
fun ObjectProgramCard(
    objectName: String,
    starVm: StarViewModel = hiltViewModel(),
    eduVm: EducationViewModel = hiltViewModel(),
    commentsVm: CommentsViewModel = hiltViewModel(),
    onProgramClick: (EducationResponse) -> Unit
) {
    val eduState by starVm.programsState.collectAsState()
    LaunchedEffect(objectName) {
        starVm.loadObjectPrograms(objectName)
    }

    when (eduState) {
        is UiState.Loading -> {
            Text("교육 프로그램을 불러오는 중...", color = TextDisabled)
            Spacer(Modifier.height(8.dp))
        }

        is UiState.Error -> {
            Text("교육 프로그램 조회 실패", color = TextDisabled)
        }

        is UiState.Success -> {
            val apiList = (eduState as UiState.Success<List<EducationResponse>>).data

            if (apiList.isEmpty()) {
                Text("이 천체에 대한 교육 프로그램이 아직 없어요", color = TextDisabled)
                Spacer(Modifier.height(8.dp))
            } else {
                val commentCounts by commentsVm.commentCounts.collectAsState()
                val uiPrograms = remember(apiList, commentCounts) {
                    apiList.map { resp ->
                        val base = resp.toEduProgram().asReview() // id = "program:<id>"
                        val syncedCount = commentCounts[base.id] ?: base.commentCount
                        base.copy(commentCount = syncedCount)
                    }
                }
                var pending by remember { mutableStateOf<EducationResponse?>(null) }
                val detailState by eduVm.detail.collectAsState()

                LaunchedEffect(detailState, pending) {
                    if (pending != null && detailState is UiState.Success<*>) {
                        onProgramClick(pending!!)
                        pending = null
                    }
                }

                ReviewSection(
                    title = "교육 프로그램",
                    reviews = uiPrograms,
                    onSyncReviewLikeCount = { _, _ -> },
                    onReviewClick = { clicked ->
                        // 클릭된 UI → 원본 EducationResponse 찾기
                        val programId = clicked.id.removePrefix("program:")
                        val origin = apiList.firstOrNull { it.id.toString() == programId } ?: return@ReviewSection

                        pending = origin
                        eduVm.loadEducationDetail(origin.id)
                    },
                    onToggleLike = { /* 필요 시 Education 좋아요 처리 */ }
                )
            }
        }
        else -> Unit
    }
}