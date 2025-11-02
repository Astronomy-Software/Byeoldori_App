package com.example.byeoldori.skymap.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.ui.theme.Purple800
import com.example.byeoldori.ui.theme.TextHighlight


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
