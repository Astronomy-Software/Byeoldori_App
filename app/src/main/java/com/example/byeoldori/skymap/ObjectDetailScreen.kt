package com.example.byeoldori.skymap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.data.model.dto.EducationResponse
import com.example.byeoldori.data.model.dto.ReviewDetailResponse
import com.example.byeoldori.data.model.dto.ReviewResponse
import com.example.byeoldori.domain.Community.EduProgram
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.skymap.components.ObjectInfoCard
import com.example.byeoldori.skymap.components.ObjectProgramCard
import com.example.byeoldori.skymap.components.ObjectReviewCard
import com.example.byeoldori.skymap.components.RealtimeInfoList
import com.example.byeoldori.skymap.components.WikipediaCard
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.components.community.program.EduProgramDetail
import com.example.byeoldori.ui.components.community.program.toEduProgram
import com.example.byeoldori.ui.components.community.review.ReviewDetail
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.utils.SweObjUtils
import com.example.byeoldori.viewmodel.Community.CommentsViewModel
import com.example.byeoldori.viewmodel.Community.EducationViewModel
import com.example.byeoldori.viewmodel.Community.ReviewViewModel

@Composable
fun ObjectDetailScreen(
    viewModel: ObjectDetailViewModel = hiltViewModel(),
    //onOpenReviewDetail: (Triple<Review, ReviewResponse?, ReviewDetailResponse?>) -> Unit = {}
) {
    val detail by viewModel.selectedObject.collectAsState()
    val realtimeItems by viewModel.realtimeItems.collectAsState()
    val isVisible by viewModel.isDetailVisible.collectAsState()

    val userVm: UserViewModel = hiltViewModel()
    val me = userVm.userProfile.collectAsState().value
    val currentUserName = me?.nickname ?: me?.name ?: "익명"
    val currentUserId = me?.id

    if (!isVisible) return
    var selectedReviewTriple by remember { mutableStateOf<Triple<Review, ReviewResponse?, ReviewDetailResponse?>?>(null) }
    var selectedProgram by remember { mutableStateOf<EduProgram?>(null) }

    val reviewVm: ReviewViewModel = hiltViewModel()
    val eduVm: EducationViewModel = hiltViewModel()
    val commentsVm: CommentsViewModel = hiltViewModel()

    selectedReviewTriple?.let { triple ->
        val ui: Review = triple.first
        val apiPost: ReviewResponse? = triple.second
        val apiDetail: ReviewDetailResponse? = triple.third

        Background(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
        ) {
            ReviewDetail(
                review = ui,
                apiPost = apiPost,
                apiDetail = apiDetail,
                currentUser = currentUserName,
                currentUserId = currentUserId,
                onSyncReviewLikeCount = { _, _, _ -> },
                onBack = { selectedReviewTriple = null },
                vm = reviewVm,
                commentsVm = commentsVm,
                onEdit = true,
            )
        }
        return
    }

    selectedProgram?.let { program ->
        Background(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
        ) {
            EduProgramDetail(
                program = program,
                onBack = { selectedProgram = null },
                vm = eduVm
            )
        }
        return
    }

    Background(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(20.dp))
            TopBar(
                title = "천체 상세 정보",
                onBack = { viewModel.setDetailVisible(false) }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (detail != null) {
                    // 상단 기본 정보
                    ObjectInfoCard(
                        name = SweObjUtils.toKorean(detail!!.name),
                        type = detail!!.type,
                        otherNames = detail!!.otherNames
                    )

                    // 실시간 정보 (카드 여러 개)
                    RealtimeInfoList(items = realtimeItems)

                    // 맨 아래 위키 요약
                    WikipediaCard(summary = detail!!.wikipediaSummary , name = detail!!.name)

                    //관측 후기들
                    ObjectReviewCard(
                        objectName = detail!!.name,
                        onReviewClick = { triple ->
                            selectedReviewTriple = triple
                        }
                    )

                    //교육 프로그램들
                    ObjectProgramCard(
                        objectName = detail!!.name,
                        onProgramClick = { programResp: EducationResponse ->
                            // EducationResponse -> EduProgram 변환
                            selectedProgram = programResp.toEduProgram()
                        }
                    )
                } else {
                    Text(
                        text = "천체 정보 없음",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
//                TestSweScreen()
                // TODO : PagerSection 추가해서 , 해당천체 이름으로 검색후 화면에 띄워주면됨 해당천체 이름은
                // TODO : 해당천체 이름은 !!detail.name으로 가져올수있음.
            }
        }
    }
}
