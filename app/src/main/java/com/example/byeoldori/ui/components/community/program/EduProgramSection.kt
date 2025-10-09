package com.example.byeoldori.ui.components.community.program

import androidx.compose.runtime.Composable
import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.byeoldori.R
import com.example.byeoldori.data.model.dto.EducationResponse
import com.example.byeoldori.data.model.dto.SortBy
import com.example.byeoldori.ui.components.community.EditorItem
import com.example.byeoldori.ui.components.community.SortBar
import com.example.byeoldori.ui.components.observatory.ReviewCard
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Community.EduProgram
import com.example.byeoldori.domain.Content
import com.example.byeoldori.domain.Observatory.Review
import com.example.byeoldori.ui.components.community.freeboard.formatCreatedAt
import com.example.byeoldori.viewmodel.EducationViewModel
import com.example.byeoldori.viewmodel.UiState
import com.example.byeoldori.viewmodel.dummyProgramComments

// EduProgram → Review 변환 (임시 어댑터)
fun EduProgram.asReview(): Review =
    Review(
        id = "program:$id",
        title = title,
        author = author,
        rating = rating.toInt(),
        likeCount = likeCount,
        commentCount = dummyProgramComments.count { it.reviewId == id },
        profile = R.drawable.profile1,
        viewCount = viewCount,
        createdAt = formatCreatedAt(createdAt),
        target = "",
        site = "",
        date = "",
        equipment = "",
        startTime = "",
        endTime = "",
        siteScore = 0,
        contentItems =  contentItems,
        liked = liked
    )


fun EducationResponse.toEduProgram(): EduProgram {
    return EduProgram(
        id = id.toString(),
        title = title,
        author = authorNickname ?: "익명",
        rating = 0f, // 교육에는 별점이 없으므로 0 기본값
        likeCount = likeCount,
        commentCount = commentCount,
        viewCount = viewCount,
        profile = R.drawable.profile1,
        createdAt = formatCreatedAt(createdAt),
        contentItems = listOf(Content.Text(contentSummary.orEmpty())),
        liked = liked
    )
}

// 정렬 타입
enum class EduProgramSort(val label: String) {
    Latest("최신순"), Like("좋아요순"), View("조회수순")
}

@Composable
fun EduProgramSection(
    eduProgramsAll: List<EduProgram>,
    onWriteClick: () -> Unit = {},
    onClickProgram: (String) -> Unit = {},
    vm: EducationViewModel = hiltViewModel()
) {
    var searchText by remember { mutableStateOf("") } //초기값이 빈 문자열인 변할 수 있는 상태 객체
    val gridState = rememberLazyGridState()

    val state by vm.postsState.collectAsState()
    val currentSort by vm.sort.collectAsState()

    val uiSort = when (currentSort) {
        SortBy.LATEST -> EduProgramSort.Latest
        SortBy.LIKES  -> EduProgramSort.Like
        SortBy.VIEWS  -> EduProgramSort.View
    }

    // API 응답을 EduProgram으로 매핑
    val apiList by remember(state) {
        mutableStateOf(
            when (state) {
                is UiState.Success ->
                    (state as UiState.Success<List<EducationResponse>>)
                        .data.map { it.toEduProgram() }
                else -> emptyList()
            }
        )
    }

    // 검색 필터링
    val filtered by remember(searchText, apiList) {
        mutableStateOf(
            if (searchText.isBlank()) apiList
            else {
                val k = searchText.trim().lowercase()
                apiList.filter { p ->
                    p.title.lowercase().contains(k) ||
                            p.author.lowercase().contains(k) ||
                            p.contentItems
                                .filterIsInstance<Content.Text>()
                                .any { it.text.lowercase().contains(k) }
                }
            }
        )
    }

    LaunchedEffect(uiSort) {
        gridState.scrollToItem(0)
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
            Spacer(Modifier.height(10.dp))
            // 정렬 칩
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 정렬 바
                SortBar(
                    current = uiSort,
                    options = EduProgramSort.entries.toList(),
                    label = { it.label },
                    onSelect = {
                        val serverSort = when (it) {
                            EduProgramSort.Latest -> SortBy.LATEST
                            EduProgramSort.Like   -> SortBy.LIKES
                            EduProgramSort.View   -> SortBy.VIEWS
                        }
                        vm.setSort(serverSort)
                    }
                )
            }
            Spacer(Modifier.height(12.dp))

            when (state) {
                is UiState.Loading -> {
                    Text("로딩 중…", style = MaterialTheme.typography.bodyMedium)
                }
                is UiState.Error -> {
                    val msg = (state as UiState.Error).message
                    Text("에러: $msg", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                }
                is UiState.Success -> {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2), //컬럼 개수 2개
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = filtered,
                            key = { it.id }
                        ) { program ->
                            Box(
                                Modifier
                                    .clickable { onClickProgram(program.id) }
                            ) {
                                ReviewCard(
                                    review = program.asReview(),
                                    onToggleLike = {
                                        // 서버 토글 + VM 상태 갱신 → 재조합되며 liked/likeCount 반영
                                        vm.toggleLike(program.id.toLong())
                                    }
                                )
                            }
                        }
                        item { Spacer(Modifier.height(60.dp)) }
                    }
                }
                UiState.Idle -> Unit
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
                tint = Color.White,    // 아이콘 흰색
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(
    name = "EduProgramSection – 기본",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360,
    heightDp = 640
)
@Composable
private fun Preview_EduProgramSection_Default() {
    MaterialTheme {
        val dummy = remember {
            (0 until 12).map { i ->
                EduProgram(
                    id = "p$i",
                    title = "천체 관측 교육 $i",
                    author = "astro$i",
                    rating = 3.5f + (i % 3) * 0.5f, // 3.5~4.5
                    likeCount = 40 + i * 3,
                    commentCount = 8 + i,
                    viewCount = 200 + i * 15,
                    profile = R.drawable.profile1,
                    createdAt = "25.10${29-1}", //String으로 변환
                    contentItems = listOf(
                       Content.Text("이 강의는 망원경 기초와 관측 매너를 다룹니다.")
                    ),
                    liked = i % 3 == 0
                )
            }
        }
        EduProgramSection(
            eduProgramsAll = dummy,
            onWriteClick = {},
            onClickProgram = {}
        )
    }
}
