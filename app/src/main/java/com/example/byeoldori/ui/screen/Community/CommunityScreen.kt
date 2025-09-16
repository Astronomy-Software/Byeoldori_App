package com.example.byeoldori.ui.screen.Community

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.components.community.freeboard.FreeBoardSection
import com.example.byeoldori.ui.components.community.program.EduProgramSection
import com.example.byeoldori.ui.components.community.review.ReviewDetail
import com.example.byeoldori.ui.components.community.review.ReviewSection
import com.example.byeoldori.ui.components.community.review.ReviewWriteForm
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Observatory.*

// --- 탭 정의 ---
enum class CommunityTab(val label: String, val routeSeg: String) {
    Home("홈","home"),
    Program("교육 프로그램", "program"),
    Review("관측 후기", "review"),
    Board("자유게시판", "board")
}

@Composable
fun CommunityScreen(
    tab: CommunityTab,
    onSelectTab: (CommunityTab) -> Unit,
    onOpenPost: (String) -> Unit = {},
) {
    val tabs = CommunityTab.entries
    var showWriteForm by remember { mutableStateOf(false) }
    val reviews = remember { mutableStateListOf<Review>().apply { addAll(dummyReviews) } }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastSubmittedReview by remember { mutableStateOf<Review?>(null) }
    var selectedReview by remember { mutableStateOf<Review?>(null) }


    when {
        showWriteForm -> {
            // 작성 화면만 표시 (탭/목록 숨김)
            ReviewWriteForm(
                author = "astro_user",
                onCancel = { showWriteForm = false },   // 취소 → 다시 탭 화면으로
                onSubmit = { showWriteForm = false },   // 등록 → 저장 처리 후 목록으로
                onTempSave = {},
                onMore = { /* 더보기 */ },
                onSubmitReview = { newReview ->
                    reviews.add(0, newReview)    // 최신 리뷰가 맨 위로
                    dummyReviews.add(0, newReview) //더미 데이터에도 추가
                    lastSubmittedReview = newReview
                    showWriteForm = false        // 작성창 닫기
                    showSuccessDialog = true
                },
                initialReview = lastSubmittedReview
            )
        }

        selectedReview != null -> {
            ReviewDetail(
                review = selectedReview!!,
                onBack = { selectedReview = null }  // 뒤로가기 누르면 다시 목록으로
            )
        }

        else -> {
            Column(Modifier.fillMaxSize()) {
                // 탭바
                Column(
                    modifier = Modifier
                        .background(Blue800)
                ) {
                    Spacer(Modifier.height(24.dp))
                    ScrollableTabRow(
                        selectedTabIndex = tabs.indexOf(tab),
                        edgePadding = 0.dp,
                        containerColor = Blue800,
                        indicator = {} //강조선 제거
                    ) {
                        tabs.forEach { t ->
                            Tab(
                                selected = (t == tab),
                                onClick = { if (t != tab) onSelectTab(t) },
                                text = {
                                    Text(
                                        text = t.label,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                                        color = if (t == tab) TextHighlight else TextDisabled
                                    )
                                }
                            )
                        }
                    }
                }
                when (tab) {
                    CommunityTab.Review -> {
                        ReviewSection(
                            reviewsAll = reviews,
                            onWriteClick = { showWriteForm = true },
                            onReviewClick = { review ->
                                selectedReview = review
                            }
                        )
                    }

                    CommunityTab.Program -> {
                        EduProgramSection(eduProgramsAll = dummyPrograms, onWriteClick = {})
                    }

                    CommunityTab.Home -> {
                        val recentReviews by remember {
                            derivedStateOf { reviews.sortedByDescending { it.createdAt }.take(8) }
                        }
                        val recentPrograms =
                            remember { dummyPrograms.sortedByDescending { it.createdAt }.take(8) }
                        val popularFreePost =
                            remember { dummyFreePosts.sortedByDescending { it.likeCount }.take(8) }

                        HomeSection(
                            recentReviews = recentReviews,
                            recentEduPrograms = recentPrograms,
                            popularFreePosts = popularFreePost
                        )
                    }

                    CommunityTab.Board -> {
                        FreeBoardSection(
                            freeBoardsAll = dummyFreePosts,
                            onClickProgram = { id -> onOpenPost(id) },
                            onWriteClick = {}
                        )
                    }
                }
            }
        }
    }
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("확인")
                }
            },
            title = { Text("알림",color = Color.Black) },
            text = { Text("리뷰가 등록되었습니다") }
        )
    }
}

// --- UI 구성 요소 ---
private data class Post(
    val id: String,
    val title: String,
    val author: String,
    val like: Int,
    val comment: Int,
    val thumbnailRes: Int? = null
)