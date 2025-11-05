package com.example.byeoldori.ui.components.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.PlanViewModel
import com.example.byeoldori.viewmodel.UiState
import java.time.*
import java.time.format.DateTimeFormatter

private fun parseDateTimeFlexible(raw: String): LocalDateTime {
    runCatching { return ZonedDateTime.parse(raw).toLocalDateTime() }
    runCatching { return OffsetDateTime.parse(raw).toLocalDateTime() }
    runCatching { return LocalDateTime.parse(raw) }
    val noSec = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    runCatching { return LocalDateTime.parse(raw, noSec) }
    return LocalDateTime.now()
}

private fun makeDisplayDate(startAt: String, endAt: String): String {
    val s = parseDateTimeFlexible(startAt)
    val e = parseDateTimeFlexible(endAt)
    val d = DateTimeFormatter.ofPattern("yy.MM.dd")
    val t = DateTimeFormatter.ofPattern("HH:mm")
    return "${s.format(d)} ${s.format(t)} ~ ${e.format(t)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetail(
    plan: PlanDetailDto,
    onBack: () -> Unit = {},
    planVm: PlanViewModel = hiltViewModel()
) {
    val detailState by planVm.detailState.collectAsState()
    val currentPlan = when (val s = detailState) {
        is UiState.Success -> s.data
        else -> plan
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    title = {
                        Text(
                            "관측 계획 상세보기",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_before),
                                contentDescription = "뒤로가기",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                )
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.6f),
                    thickness = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = currentPlan.title.ifBlank { "관측 계획" },
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextHighlight,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(12.dp))

            LabeledField("관측 일시", makeDisplayDate(currentPlan.startAt, currentPlan.endAt))
            LabeledField("관측 대상", currentPlan.targets?.joinToString(", ") ?: "—")
            LabeledField("관측지", currentPlan.placeName ?: currentPlan.observationSiteName ?: "—")

            HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 2.dp)

            Spacer(Modifier.height(16.dp))

            if (!currentPlan.memo.isNullOrBlank()) {
                LabeledField("메모", currentPlan.memo!!)
            }

            val photoUrls = currentPlan.photos?.mapNotNull { it.url } ?: emptyList()
            if (photoUrls.isNotEmpty()) {
                ContentInput(
                    items = photoUrls.map { EditorItem.Photo(model = it) },
                    onItemsChange = {},
                    onPickImages = {},
                    onCheck = {},
                    onChecklist = {},
                    readOnly = true
                )
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = TextDisabled,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value.ifBlank { "—" },
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(10.dp))
    }
}

private fun dummyPlan(): PlanDetailDto = PlanDetailDto(
    id = 3L,
    title = "오리온자리 관측하기",
    startAt = "2025-11-04T21:00",
    endAt   = "2025-11-04T22:00",
    targets = listOf("오리온자리"),
    observationSiteId = 3L,
    observationSiteName = "충북대 대운동장",
    lat = 36.62599134299997,
    lon = 127.46185397919821,
    placeName = "충북대 대운동장",
    status = EventStatus.PLANNED,
    memo = "오리온대(벨트)와 M42를 중심으로 관측 예정. 간단 스케치 후 비교.",
    photos = emptyList(),
    createdAt = "",
    updatedAt = ""
)

private fun dummyPlanNoPhoto(): PlanDetailDto =
    dummyPlan().copy(photos = emptyList(), memo = "")

@Preview(
    name = "PlanDetail - No Photo",
    showBackground = true,
    backgroundColor = 0xFF241860,
    widthDp = 360, heightDp = 720
)
@Composable
private fun Preview_PlanDetail_NoPhoto() {
    PlanDetail(plan = dummyPlanNoPhoto())
}