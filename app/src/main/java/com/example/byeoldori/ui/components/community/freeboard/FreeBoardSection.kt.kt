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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.components.community.EditorItem
import com.example.byeoldori.ui.components.community.SortBar
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Community.FreePost
import com.example.byeoldori.viewmodel.Observatory.Review

enum class FreeBoardSort(val label: String) {
    Latest("최신순"), Like("좋아요순"), View("조회수순")
}

fun FreePost.asReview(): Review =
    Review(
        id = id,
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
        contentItems = buildList {
            add(EditorItem.Paragraph(value = TextFieldValue(content ?: "")))
            imageRes?.let { add(EditorItem.Photo(model = it)) }
        }
    )

@Composable
fun FreeBoardSection(
    freeBoardsAll: List<FreePost>,
    onWriteClick: () -> Unit = {},
    onClickProgram: (String) -> Unit = {}
){
    var searchText by remember { mutableStateOf("") } //초기값이 빈 문자열인 변할 수 있는 상태 객체
    var sort by remember { mutableStateOf(FreeBoardSort.Latest) }
    val listState = rememberLazyListState()

    // 검색 + 정렬 적용
    val filtered = remember(searchText, sort, freeBoardsAll) {
        val q = searchText.trim() //양끝 공백 제거
        val base = if (q.isEmpty()) freeBoardsAll //검색어가 비어있으면 전체 리스트 사용
        else {
            freeBoardsAll.filter { r -> //제목,작성자를 검색(대소문자 구분X)
                r.title.contains(q, ignoreCase = true) ||
                        r.author.contains(q, ignoreCase = true)
            }
        }
        when (sort) {
            FreeBoardSort.Latest -> base.sortedByDescending { it.createdAt}
            FreeBoardSort.Like -> base.sortedByDescending { it.likeCount }
            FreeBoardSort.View -> base.sortedByDescending { it.viewCount }
        }
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
                    current = sort,
                    options = FreeBoardSort.entries.toList(),
                    label = { it.label },
                    onSelect = { sort = it }
                )
            }
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { post ->
                    Column {
                        FreeBoardItem(
                            post = post,
                            onClick = { onClickProgram(post.id) }
                        )
                        Divider(
                            color = Color.White.copy(alpha = 0.8f),
                            thickness = 1.dp)
                    }
                }
            }
        }
        //리뷰 작성 버튼
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

