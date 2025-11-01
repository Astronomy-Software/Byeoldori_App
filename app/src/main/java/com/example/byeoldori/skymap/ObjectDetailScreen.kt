package com.example.byeoldori.skymap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.skymap.components.ObjectInfoCard
import com.example.byeoldori.skymap.components.RealtimeInfoList
import com.example.byeoldori.skymap.components.WikipediaCard
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.utils.SweObjUtils

@Composable
fun ObjectDetailScreen(
    viewModel: ObjectDetailViewModel = hiltViewModel()
) {
    val detail by viewModel.selectedObject.collectAsState()
    val realtimeItems by viewModel.realtimeItems.collectAsState()
    val isVisible by viewModel.isDetailVisible.collectAsState()

    if (!isVisible) return

    Background(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
