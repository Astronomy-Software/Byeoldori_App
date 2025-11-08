package com.example.byeoldori.ui.components.community.freeboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.BuildConfig
import com.example.byeoldori.R
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.ui.components.community.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Community.FreePost
import com.example.byeoldori.domain.Content
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.viewmodel.*
import com.example.byeoldori.viewmodel.Community.CommentsViewModel
import com.example.byeoldori.viewmodel.Community.CommunityViewModel
import java.time.LocalDateTime
import java.time.format.*

enum class FreeBoardSort(val label: String) {
    Latest("최신순"), Like("좋아요순"), View("조회수순")
}

fun formatCreatedAt(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""

    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", // ← 현재 서버 포맷 (마이크로초 6자리)
        "yyyy-MM-dd'T'HH:mm:ss.SSS",    // 밀리초 3자리
        "yyyy-MM-dd'T'HH:mm:ss",        // 초까지
        "yyyyMMddHHmm"                  // 예전 형태 (중기예보 등)
    )

    for (pattern in patterns) {
        try {
            val date = LocalDateTime.parse(createdAt, DateTimeFormatter.ofPattern(pattern))
            return date.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
        } catch (_: DateTimeParseException) {
            continue
        }
    }

    // 모든 포맷 실패 시 원문 반환
    return createdAt
}

fun FreePost.asReview(): Review =
    Review(
        id = this.id,
        title = title,
        author = author ?: "익명",
        rating = 0,              // 자유게시판엔 별점이 없으니 0으로
        likeCount = likeCount,
        commentCount = commentCount,
        authorProfileImageUrl = authorProfileImageUrl,
        viewCount = viewCount,
        createdAt = createdAt,
        targets = emptyList(),
        site = "",
        date = "",
        equipment = "",
        siteScore = 0,
        contentItems = contentItems,
        liked = this.liked
    )

fun FreePostResponse.toFreePost(): FreePost {
    val formattedDate = formatCreatedAt(createdAt)
    val validThumbnail = if (!thumbnailUrl.isNullOrBlank() &&
        (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://"))
    ) {
        thumbnailUrl
    } else {
        "android.resource://${BuildConfig.APPLICATION_ID}/${R.drawable.img_dummy}"
    }

    return FreePost(
        id = id.toString(),
        title = title,
        author = authorNickname ?: "익명",
        authorId = authorId,
        likeCount = likeCount,
        commentCount = commentCount,
        viewCount = viewCount,
        createdAt = formattedDate,
        contentItems = listOf(Content.Text(contentSummary)),
        profile = null,
        liked = liked,
        thumbnail = validThumbnail,
        authorProfileImageUrl = authorProfileImageUrl
    )
}

@Composable
fun FreeBoardSection(
    freeBoardsAll: List<FreePost>,
    onWriteClick: () -> Unit = {},
    onClickPost: (String) -> Unit = {},
    currentSort: SortBy,
    onChangeSort: (SortBy) -> Unit = {},
    vm: CommunityViewModel = hiltViewModel()
){
    var searchText by remember { mutableStateOf("") } //초기값이 빈 문자열인 변할 수 있는 상태 객체
    var sort by remember { mutableStateOf(FreeBoardSort.Latest) }
    val listState = rememberLazyListState()

    val vmPostsState by (vm?.postsState?.collectAsState()
        ?: remember { mutableStateOf<UiState<List<FreePostResponse>>>(UiState.Idle) })

    val posts: List<FreePost> = when (val s = vmPostsState) {
        is UiState.Success -> s.data.map { it.toFreePost() }   // <-- 최신 값
        else -> emptyList()
    }

    //val commentCountsByVm by vm.commentCounts.collectAsState()
    //val commentOverrides by vm.freeCommentOverrides.collectAsState()
    val commentsVm: CommentsViewModel = hiltViewModel()
    val commentCounts by commentsVm.commentCounts.collectAsState()

    // 검색만
    val filtered = run {
        val q = searchText.trim()
        if (q.isEmpty()) posts else {
            posts.filter { r ->
                val k = q.lowercase()
                r.title.contains(q, ignoreCase = true) ||
                        (r.author?.contains(q, ignoreCase = true) == true) ||
                        r.contentItems.filterIsInstance<EditorItem.Paragraph>()
                            .any { it.value.text.lowercase().contains(k) }
            }
        }
    }

    val uiSort = when (currentSort) {
        SortBy.LATEST -> FreeBoardSort.Latest
        SortBy.LIKES  -> FreeBoardSort.Like
        SortBy.VIEWS  -> FreeBoardSort.View
    }

    LaunchedEffect(sort) {
        listState.scrollToItem(0)
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            SearchBar(
                searchQuery = searchText,
                onSearch = { searchText = it }
            )
            Spacer(Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 정렬 바
                SortBar(
                    current = uiSort,
                    options = FreeBoardSort.entries.toList(),
                    label = { it.label },
                    onSelect = {
                        val serverSort = when (it) {
                            FreeBoardSort.Latest -> SortBy.LATEST
                            FreeBoardSort.Like   -> SortBy.LIKES
                            FreeBoardSort.View   -> SortBy.VIEWS
                        }
                        onChangeSort(serverSort)
                    }
                )
            }
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { post ->
                    //val uiCommentCount = commentOverrides[post.id] ?: post.commentCount
                    val uiCommentCount = commentCounts[post.id] ?: post.commentCount
                    Column {
                        FreeBoardItem(
                            post = post,
                            commentCount = uiCommentCount,
                            likeCount = post.likeCount,
                            isLiked = post.liked,
                            onClick = { onClickPost(post.id) },
                            onLikeClick = { vm.toggleLike(post.id.toLong()) }
                        )
                        Divider(color = Color.White.copy(alpha = 0.8f), thickness = 1.dp)
                    }
                }
            }
        }
        //작성 버튼
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = -(24.dp), y= -(10.dp))
                .size(56.dp)
                .clickable {
                    vm.clearCreateState()
                    onWriteClick()
                }
                .background(Blue800, shape = CircleShape) // 보라색 배경
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_notebook_pen),
                contentDescription = "글쓰기",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FreeGrid(
    posts: List<FreePost>,
    onClick: (FreePost) -> Unit,
    onToggle: (String) -> Unit
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            Column {
                FreeBoardItem(
                    post = post,
                    commentCount = post.commentCount,
                    likeCount = post.likeCount,
                    isLiked = post.liked,
                    onClick = { onClick(post) },
                    onLikeClick = { onToggle(post.id) }
                )
                Divider(color = Color.White.copy(alpha = 0.8f), thickness = 1.dp)
            }
        }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF241860, widthDp = 420, heightDp = 840)
@Composable
private fun Preview_FreeBoardSection_Empty() {
    MaterialTheme {
        FreeBoardSection(
            freeBoardsAll = dummyFreePosts,
            onWriteClick = {},
            onClickPost = {},
            currentSort = SortBy.LATEST,
            onChangeSort = {}
        )
    }
}