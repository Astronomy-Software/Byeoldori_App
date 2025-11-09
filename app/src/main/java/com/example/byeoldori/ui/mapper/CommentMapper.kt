package com.example.byeoldori.ui.mapper

import android.util.Log
import com.example.byeoldori.R
import com.example.byeoldori.data.model.dto.CommentResponse
import com.example.byeoldori.domain.Community.ReviewComment
import com.example.byeoldori.ui.components.community.freeboard.formatCreatedAt
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField


private val OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")
private val KST = ZoneId.of("Asia/Seoul")

private const val TAG = "CommentTimeFmt"

private val ISO_LOCAL_FLEX: DateTimeFormatter =
    DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        .optionalStart()
        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
        .optionalEnd()
        .toFormatter()

fun formatServerDateKst(raw: String?): String {
    val s = raw?.trim().orEmpty()
    if (s.isEmpty()) {
        Log.w(TAG, "empty input -> --:--")
        return "--:--"
    }
    Log.d(TAG, "input='$s'")

    // 0) epoch 숫자 (millis/seconds)
    if (s.all { it.isDigit() }) {
        return runCatching {
            val millis = when {
                s.length >= 13 -> s.toLong()          // millis
                s.length >= 10 -> s.toLong() * 1000   // seconds
                else -> return "--:--"
            }
            val out = OUTPUT_FORMATTER.format(Instant.ofEpochMilli(millis).atZone(KST))
            Log.d(TAG, "parsed as epoch millis: $millis -> $out")
            out
        }.getOrElse {
            Log.w(TAG, "epoch parse fail: ${it.message}")
            "--:--"
        }
    }

    // 1) Z(UTC)
    runCatching {
        val instant = Instant.parse(s)
        val out = OUTPUT_FORMATTER.format(instant.atZone(KST))
        Log.d(TAG, "parsed as ISO_INSTANT(Z): $out")
        return out
    }.onFailure { /* 무시하고 다음 후보 시도 */ }

    // 2) 오프셋 포함 ISO
    runCatching {
        val odt = OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val out = OUTPUT_FORMATTER.format(odt.atZoneSameInstant(KST))
        Log.d(TAG, "parsed as ISO_OFFSET_DATE_TIME: $out")
        return out
    }.onFailure { /* 다음 */ }

    // 3) 오프셋 없는 ISO (fraction 1~9)
    runCatching {
        val ldt = LocalDateTime.parse(s, ISO_LOCAL_FLEX)
        val out = OUTPUT_FORMATTER.format(ldt.atZone(KST))
        Log.d(TAG, "parsed as ISO_LOCAL_FLEX: $out")
        return out
    }.onFailure { /* 다음 */ }

    // 4) 프리뷰/기타 포맷
    val candidates = listOf(
        DateTimeFormatter.ofPattern("yyyyMMddHHmm")
    )
    for (fmt in candidates) {
        runCatching {
            val ldt = LocalDateTime.parse(s, fmt)
            val out = OUTPUT_FORMATTER.format(ldt.atZone(KST))
            Log.d(TAG, "parsed as custom '$fmt': $out")
            return out
        }
    }

    Log.w(TAG, "all parse attempts failed for '$s' -> --:--")
    return "--:--"
}

fun CommentResponse.toUi(postId: Long): ReviewComment {
    return ReviewComment(
        id = id,
        reviewId = postId,           // 게시물(프로그램/리뷰/자유) 공용 id 문자열
        authorId = authorId,                 // 서버에서 닉네임을 추가로 내려주면 채워 넣기
        authorNickname = authorNickname,
        authorProfileImageUrl = authorProfileImageUrl,
        content = content,
        likeCount = likeCount,
        commentCount = 0,
        createdAt = formatServerDateKst(createdAt),
        parentId = parentId,
        liked = liked,
        deleted = deleted
    )
}