package com.example.byeoldori.skymap

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.skymap.viewmodel.ObjectDetailViewModel
import com.example.byeoldori.skymap.viewmodel.ObjectItem
import com.example.byeoldori.ui.components.TopBar
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.ui.theme.Purple800
import com.example.byeoldori.ui.theme.TextHighlight
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
                    CelestialInfoCard(
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
                TestSweScreen()
            }
        }
    }
}
@Composable
fun CelestialInfoCard(
    name: String,
    type: String,
    otherNames: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    val joinedNames = if (otherNames.isNotEmpty()) otherNames.joinToString(", ") else "없음"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Purple800)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 이름
            Text(
                text = name,
                fontSize = 32.sp,
                color = TextHighlight,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))
            Text("종류: $type", color = TextHighlight)
            Spacer(Modifier.height(4.dp))

            Text(
                text = "다른 이름 : $joinedNames",
                color = TextHighlight,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            // "더보기/접기" 토글 버튼
            if (joinedNames.length > 30 || otherNames.size > 2) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (expanded) "접기 ▴" else "더보기 ▾",
                    color = Color.LightGray,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun RealtimeInfoList(items: List<ObjectItem>) {
    if (items.isEmpty()) {
        Text("실시간 데이터 없음", color = Color.LightGray)
        return
    }

    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            RealtimeItemCard(
                item = item,
                modifier = Modifier
                    .fillMaxWidth(0.49f) // ✅ 화면 너비의 절반으로 강제
                    .heightIn(min = 100.dp)
            )
        }
    }
}


@Composable
fun RealtimeItemCard(
    item: ObjectItem,
    modifier: Modifier = Modifier
) {
    // 🔹 공백 3개 기준 분할
    val splitValues = remember(item.value) {
        if (item.value.contains("   ")) {
            item.value.split("   ").map { it.trim() }
        } else {
            listOf(item.value)
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Purple800),
        modifier = modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Key
            Text(
                text = item.key,
                color = TextHighlight,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Value (세로 배치)
            splitValues.forEach { part ->
                Text(
                    text = part,
                    color = TextHighlight,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun WikipediaCard(summary: String, name: String) {
    val context = LocalContext.current
    val wikiUrl = "https://en.wikipedia.org/wiki/" + name.replace("NAME ", "")

    // 텍스트 안에 "more on wikipedia"가 포함되어 있는지 확인
    val hasWikiLink = summary.contains("more on wikipedia", ignoreCase = true)
    val displayText = if (hasWikiLink)
        summary.substringBefore("more on wikipedia").trimEnd()
    else
        summary.trim()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Purple800)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "위키백과 요약",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextHighlight
                )
            )
            Spacer(Modifier.height(8.dp))

            // 요약 본문 출력
            Text(
                text = displayText.ifBlank { "요약 정보 없음" },
                color = TextHighlight,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            if (hasWikiLink) { // TODO : 위키피디아 링크 보내는거 따로 받아와야할듯?
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "🔗 More on Wikipedia",
                    color = Color.Cyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wikiUrl))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}
