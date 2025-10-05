package com.example.byeoldori.ui.components.community.freeboard

import androidx.compose.runtime.Composable
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
import com.example.byeoldori.R
import com.example.byeoldori.data.model.dto.FreePostResponse
import com.example.byeoldori.data.model.dto.SortBy
import com.example.byeoldori.ui.components.community.EditorItem
import com.example.byeoldori.ui.components.community.LikeState
import com.example.byeoldori.ui.components.community.SortBar
import com.example.byeoldori.ui.components.community.likedKeyFree
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Community.FreePost
import com.example.byeoldori.domain.Content
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.viewmodel.CommunityViewModel
import com.example.byeoldori.viewmodel.dummyFreeComments
import com.example.byeoldori.viewmodel.dummyFreePosts
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class FreeBoardSort(val label: String) {
    Latest("최신순"), Like("좋아요순"), View("조회수순")
}

fun formatCreatedAt(createdAt: String): String {
    return try {
        val parsed = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME)
        parsed.format(DateTimeFormatter.ofPattern("yy.MM.dd")) // "25.10.03"
    } catch (e: Exception) {
        createdAt // 실패하면 원본 표시
    }
}

fun FreePost.asReview(): Review =
    Review(
        id = likedKeyFree(this.id),
        title = title,
        author = author,
        rating = 0,              // 자유게시판엔 별점이 없으니 0으로
        likeCount = likeCount,
        commentCount = commentCount,
        profile = R.drawable.profile1,
        viewCount = viewCount,
        createdAt = createdAt,
        target = "",
        site = "",
        date = "",
        equipment = "",
        startTime = "",
        endTime = "",
        siteScore = 0,
        contentItems = contentItems
    )

fun FreePostResponse.toFreePost(): FreePost {
    val formattedDate = formatCreatedAt(createdAt)

    return FreePost(
        id = id.toString(),
        title = title,
        author = authorNickname.toString(),
        likeCount = likeCount,
        commentCount = commentCount,
        viewCount = viewCount,
        createdAt = formattedDate,
        contentItems = listOf(Content.Text(contentSummary)),
        profile = null
    )
}

@Composable
fun FreeBoardSection(
    freeBoardsAll: List<FreePost>,
    onWriteClick: () -> Unit = {},
    onClickPost: (String) -> Unit = {},
    currentSort: SortBy,
    onChangeSort: (SortBy) -> Unit = {},
    vm: CommunityViewModel
){
    var searchText by remember { mutableStateOf("") } //초기값이 빈 문자열인 변할 수 있는 상태 객체
    var sort by remember { mutableStateOf(FreeBoardSort.Latest) }
    val listState = rememberLazyListState()
    val likeState by vm.likeState.collectAsState()
    val likedIds by vm.likedIds.collectAsState()

    // 검색만
    val filtered = run {
        val q = searchText.trim()
        if (q.isEmpty()) freeBoardsAll else {
            freeBoardsAll.filter { r ->
                val k = q.lowercase()
                r.title.lowercase().contains(k) ||
                        r.author.lowercase().contains(k) ||
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
            com.example.byeoldori.ui.components.community.SearchBar(
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
                    val likeKey = likedKeyFree(post.id)
                    val isLiked = likeKey in likedIds
                    val liveCommentCount = dummyFreeComments.count { it.reviewId == post.id }
                    Column {
                        FreeBoardItem(
                            post = post,
                            commentCount = liveCommentCount,
                            likeCount = post.likeCount,
                            isLiked = isLiked,
                            onClick = { onClickPost(post.id) },
                            onLikeClick = { vm.toggleLike(post.id.toLong()) }
                        )
                        Divider(
                            color = Color.White.copy(alpha = 0.8f),
                            thickness = 1.dp)
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
                .clickable(onClick = onWriteClick)
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
//
//@Preview(
//    showBackground = true,
//    backgroundColor = 0xFF241860,
//    widthDp = 420,
//    heightDp = 840
//)
//@Composable
//private fun Preview_FreeBoardSection_Empty() {
//    // ✅ HiltViewModel은 프리뷰에서 직접 생성 불가하므로 더미 구현 사용
//    val fakeVm = object : CommunityViewModel(
//        repo = com.example.byeoldori.data.repository.CommunityRepository(
//            api = com.example.byeoldori.data.api.FakeCommunityApi()
//        )
//    ) {}
//
//    MaterialTheme {
//        FreeBoardSection(
//            freeBoardsAll = dummyFreePosts,
//            onWriteClick = {},
//            onClickPost = {},
//            currentSort = SortBy.LATEST,
//            onChangeSort = {},
//            vm = fakeVm // ✅ 더미 뷰모델 전달
//        )
//    }
//}