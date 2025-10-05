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
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.community.EditorItem
import com.example.byeoldori.ui.components.community.SortBar
import com.example.byeoldori.ui.components.observatory.ReviewCard
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.domain.Community.EduProgram
import com.example.byeoldori.domain.Content
import com.example.byeoldori.domain.Observatory.Review
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
        createdAt = createdAt,
        target = "",
        site = "",
        date = "",
        equipment = "",
        startTime = "",
        endTime = "",
        siteScore = 0,
        contentItems =  contentItems
    )

// 정렬 타입
enum class EduProgramSort(val label: String) {
    Latest("최신순"), Like("좋아요순"), View("조회수순")
}

@Composable
fun EduProgramSection(
    eduProgramsAll: List<EduProgram>,
    onWriteClick: () -> Unit = {},
    onClickProgram: (String) -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") } //초기값이 빈 문자열인 변할 수 있는 상태 객체
    var sort by remember { mutableStateOf(EduProgramSort.Latest) }
    val gridState = rememberLazyGridState()

    // 검색 + 정렬 적용
    val filtered = remember(searchText, sort, eduProgramsAll) {
        val q = searchText.trim() //양끝 공백 제거
        val base = if (q.isEmpty()) eduProgramsAll //검색어가 비어있으면 전체 리스트 사용
        else {
            eduProgramsAll.filter { r -> //제목,작성자를 검색(대소문자 구분X)
                r.title.contains(q, ignoreCase = true) ||
                        r.author.contains(q, ignoreCase = true)
            }
        }
        when (sort) {
            EduProgramSort.Latest -> base.sortedByDescending { it.createdAt}
            EduProgramSort.Like -> base.sortedByDescending { it.likeCount }
            EduProgramSort.View -> base.sortedByDescending { it.viewCount }
        }
    }

    LaunchedEffect(sort) {
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
                    current = sort,
                    options = EduProgramSort.entries.toList(),
                    label = { it.label },
                    onSelect = { sort = it }
                )
            }
            Spacer(Modifier.height(12.dp))
            // 2열 그리드
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
                ) { eduProgram ->
                    ReviewCard(
                        review = eduProgram.asReview(),
                        modifier = Modifier.clickable { onClickProgram(eduProgram.id) }
                    )
                }
                item { Spacer(Modifier.height(60.dp)) }
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
                    )
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
