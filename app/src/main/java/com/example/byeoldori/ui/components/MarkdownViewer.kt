package com.example.byeoldori.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byeoldori.ui.theme.Purple900
import com.example.byeoldori.ui.theme.TextHighlight
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

@Composable
fun MarkdownViewer(
    modifier: Modifier = Modifier,
    assetFileName: String? = null,      // 예: "privacy_policy.md" (assets)
    lines: List<String>? = null,        // 프리뷰/테스트용 직접 주입
) {
    val context = LocalContext.current

    // 우선순위: lines(직접 주입) > assetFileName(assets에서 읽기)
    val contentLines: List<String> = remember(assetFileName, lines) {
        when {
            lines != null -> lines
            !assetFileName.isNullOrBlank() -> {
                runCatching {
                    context.assets.open(assetFileName).use { input ->
                        BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readLines()
                    }
                }.getOrElse { _ -> emptyList() }
            }
            else -> emptyList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()                 // ⬅ 가로는 최대
            .wrapContentHeight()            // ⬅ 세로는 내용만큼
            .border(2.dp, TextHighlight , RoundedCornerShape(10.dp))
            .background(TextHighlight)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()             // ⬅ 내부도 가로 최대
                .wrapContentHeight(),       // ⬅ 세로 랩 컨텐츠
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            contentLines.forEach { line ->
                when {
                    line.startsWith("# ") -> {
                        Text(
                            text = line.removePrefix("# ").trim(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Purple900
                        )
                    }
                    line.startsWith("## ") -> {
                        Text(
                            text = line.removePrefix("## ").trim(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple900
                        )
                    }
                    line.startsWith("### ") -> {
                        Text(
                            text = line.removePrefix("### ").trim(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Purple900
                        )
                    }
                    line.startsWith("- ") || line.startsWith("•") -> {
                        Text(
                            text = line,
                            fontSize = 14.sp,
                            color = Purple900
                        )
                    }
                    line.isBlank() -> {
                        // 빈 줄 - 적당한 여백
                        Text(text = "", fontSize = 8.sp)
                    }
                    else -> {
                        val annotatedText = buildAnnotatedString {
                            // URL / 이메일 / 전화 패턴
                            val linkPattern = Pattern.compile(
                                "(https?://\\S+)|(mailto:\\S+@[\\w.]+)|(tel:\\+?\\d+)"
                            )
                            val matcher = linkPattern.matcher(line)
                            var lastIndex = 0

                            while (matcher.find()) {
                                val start = matcher.start()
                                val end = matcher.end()

                                append(line.substring(lastIndex, start))

                                val link = line.substring(start, end)
                                pushStringAnnotation(tag = "LINK", annotation = link)
                                withStyle(
                                    SpanStyle(
                                        color = Color(0xFF64B5F6),
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) { append(link) }
                                pop()

                                lastIndex = end
                            }
                            append(line.substring(lastIndex))
                        }

                        Text(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = Purple900
                            ),
                            modifier = Modifier.clickable {
                                annotatedText.getStringAnnotations("LINK", 0, annotatedText.length)
                                    .firstOrNull()?.let { ann ->
                                        when {
                                            ann.item.startsWith("mailto:") -> {
                                                context.startActivity(
                                                    Intent(Intent.ACTION_SENDTO, Uri.parse(ann.item))
                                                )
                                            }
                                            ann.item.startsWith("tel:") -> {
                                                context.startActivity(
                                                    Intent(Intent.ACTION_DIAL, Uri.parse(ann.item))
                                                )
                                            }
                                            else -> {
                                                context.startActivity(
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(ann.item))
                                                )
                                            }
                                        }
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MarkdownViewerPreview() {
    val demo = listOf(
        "# 개인정보 처리방침",
        "본 방침은 2025년 5월 25일부터 적용됩니다.",
        "",
        "## 1. 수집하는 개인정보의 항목",
        "- 기기정보(OS 버전, 기기 모델)",
        "- 서비스 이용 기록(로그, 접속 IP, 기기 식별자)",
        "",
        "### 문의",
        "mailto:privacy@example.com / tel:+821012345678 / https://example.com"
    )
    MarkdownViewer(lines = demo)
}
