package com.example.byeoldori.ui.components.community.review

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.ReviewComment
import com.example.byeoldori.viewmodel.Observatory.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun formatCreatedAt(createdAt: Long): String {
    // createdAt이 예: 202510290000 같은 custom Long이라면 직접 파싱 필요
    return try {
        if (createdAt.toString().length == 12) {
            // "yyyyMMddHHmm" 형식으로 저장된 경우
            val sdf = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
            val date = sdf.parse(createdAt.toString())
            SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(date!!)
        } else {
            // epoch millis로 저장된 경우
            val date = Date(createdAt)
            SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        createdAt.toString() // fallback
    }
}

fun likeComment(commentId: String, delta: Int) {
    val idx = dummyReviewComments.indexOfFirst { it.id == commentId }
    if (idx >= 0) {
        val c = dummyReviewComments[idx]
        val newCount = (c.likeCount + delta).coerceAtLeast(0)
        dummyReviewComments[idx] = c.copy(likeCount = newCount)
    }
}

fun likeComment(id: String)  = likeComment(id, +1)
fun unlikeComment(id: String) = likeComment(id, -1)

fun addComment(
    reviewId: String,
    author: String,
    content: String,
    profile: Int? = R.drawable.profile1
): ReviewComment {
    val new = ReviewComment(
        id = "c${System.currentTimeMillis()}",
        reviewId = reviewId,
        author = author,
        profile = profile,
        content = content,
        likeCount = 0,
        commentCount = 0,
        createdAt = System.currentTimeMillis()
    )
    dummyReviewComments.add(new)
    return new
}

@Composable
fun rememberIsImeVisible(): Boolean {
    val density = LocalDensity.current
    // 키보드가 올라오면 bottom 값이 0보다 커짐
    return WindowInsets.ime.getBottom(density) > 0
}

@Composable
fun ReviewDetail(
    review: Review,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
    onBottomBarVisibleChange: (Boolean) -> Unit = {},
    currentUser: String
) {
    var commentText by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val imeVisible = rememberIsImeVisible()
    val tailRequester = remember { BringIntoViewRequester() }
    var requestKeyboard by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var editingTarget by remember { mutableStateOf<ReviewComment?>(null) }
    //val liked = remember { mutableStateMapOf<String, Boolean>() } //좋아요 기억
    var liked by rememberSaveable { mutableStateOf(setOf<String>()) }
    var reviewLiked by rememberSaveable { mutableStateOf(false) }
    var reviewLikeCount by rememberSaveable { mutableStateOf(review.likeCount) }


    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            // 키보드가 올라오면 리스트를 바닥까지 끌어내려 빈 여백 느낌 제거
            tailRequester.bringIntoView()
        }
    }
    LaunchedEffect(requestKeyboard) {
        if (requestKeyboard) {
            focusRequester.requestFocus()
            keyboardController?.show()
            requestKeyboard = false  // 한 번만 실행
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets(0))

    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            //.padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y=(10).dp)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_before), // ← 아이콘
                    contentDescription = "뒤로가기",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* 공유 로직 */ }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_constellation), // 공유 아이콘
                        contentDescription = "공유",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)

                    )
                }
                IconButton(onClick = { /* 더보기 로직 */ }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more), //
                        contentDescription = "더보기",
                        tint = Color.White
                    )
                }
            }
        }
        Divider(color = Color.LightGray.copy(alpha = 0.4f), thickness = 1.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text( // 제목
                text = review.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextHighlight
                )
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 프로필 이미지 (임시로 drawable 아이콘 사용)
                review.profile?.let {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = "프로필 이미지",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(50.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text( //사용자 이름
                        text = review.author,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = 17.sp,
                        color = TextHighlight
                    )
                    Spacer(Modifier.height(4.dp))
                    Text( //작성일
                        text = formatCreatedAt(review.createdAt), // createdAt 형식에 맞게 변환 필요
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                        fontSize = 17.sp
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            ReviewInput(
                target = review.target ?: "",
                onTargetChange = {},
                site = review.site ?: "",
                onSiteChange = {},
                equipment = review.equipment ?: "",
                onEquipmentChange = {},
                date = review.date ?: "",
                startTime = review.startTime ?: "",
                endTime = review.endTime ?: "",
                onTimeChange = { _, _ -> },
                rating = review.rating.toString() + "/5",
                onRatingChange = {},
                siteScore = review.siteScore.toString() + "/5",
                onSiteScoreChange = {},
                onDateChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false //수정 못하게
            )
            ContentInput(
                items = review.contentItems,
                onItemsChange = {},    // 상세에서 편집 막으려면 {} 로 바꿔도 OK
                onSubmit = {},
                onPickImages = { /* no-op */ },
                onChecklist = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.6f), thickness = 2.dp)

            //좋아요 + 댓글
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        reviewLiked = !reviewLiked
                        reviewLikeCount = (reviewLikeCount + if (reviewLiked) 1 else -1).coerceAtLeast(0)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_thumbs_up),
                        contentDescription = "좋아요",
                        tint = if (reviewLiked) Purple500 else Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "좋아요   $reviewLikeCount",
                        color = TextHighlight,
                        fontSize = 14.sp
                    )
                }
                Divider(
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .height(20.dp)
                        .width(2.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_comment),
                        contentDescription = "댓글",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))

                    //총 댓글 수
                    val commentCount = dummyReviewComments.count { it.reviewId == review.id }
                    Text(
                        text = "댓글   $commentCount",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            Divider(color = Color.White.copy(alpha = 0.6f), thickness = 2.dp)
            dummyReviewComments
                .filter { it.reviewId == review.id }
                .forEach { c ->
                    val isLiked = c.id in liked
                    CommentItem(
                        comment = c,
                        isLiked = isLiked,
                        onLike = { tapped ->
                            if(tapped.id in liked) {
                                liked = liked - tapped.id
                                unlikeComment(tapped.id)
                            } else {
                                liked = liked + tapped.id
                                likeComment(tapped.id)
                            }
                        },
                        onReply = { /* TODO: 대댓글 입력창 열기 (추후) */ },
                        canEditDelete = { it.author == currentUser },
                        onEdit = { target ->
                            if (target.author == currentUser) {
                                editingTarget = target
                                commentText = ""
                                requestKeyboard = true
                            }
                        },
                        onDelete = { del ->
                            val idx = dummyReviewComments.indexOfFirst { it.id == del.id }
                            if (idx >= 0) dummyReviewComments.removeAt(idx)
                        }
                    )
                }
            Spacer(modifier = Modifier.height(50.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .bringIntoViewRequester(tailRequester)
            )
        }
    }
        CommentInput(
            text = commentText,
            onTextChange = { commentText = it },
            onSend = { text ->
                val t = text.trim()
                val target = editingTarget

                if (target != null) {
                    val idx = dummyReviewComments.indexOfFirst { it.id == target.id }
                    if (idx >= 0) {
                        dummyReviewComments[idx] = target.copy(content = t)
                    }
                    editingTarget = null
                    commentText = ""
                    return@CommentInput
                }
                addComment(
                    reviewId = review.id,
                    author = currentUser,
                    content = t
                )
                commentText = ""
                    scope.launch {
                        // 새 댓글까지 스무스하게 스크롤
                        tailRequester.bringIntoView()
                    }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .advancedImePadding()
                .focusRequester(focusRequester)

        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 500, heightDp = 1200)
@Composable
private fun Preview_ReviewDetail() {
    MaterialTheme {
        ReviewDetail(review = dummyReviews.first(), currentUser = "astro_user")
    }
}