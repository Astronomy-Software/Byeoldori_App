package com.example.byeoldori.ui.screen.Community

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.AppTheme

// --- 탭 정의 ---
enum class CommunityTab(val label: String, val routeSeg: String) {
    Feed("피드", "feed"),
    Hot("인기", "hot"),
    My("내 글", "my")
}

@Composable
fun CommunityScreen(
    tab: CommunityTab,                       // 현재 탭 (라우트에서 결정)
    onSelectTab: (CommunityTab) -> Unit,     // 탭 클릭 시 부모로 콜백
    onOpenPost: (String) -> Unit = {}        // 게시글 클릭 콜백
) {
    val tabs = CommunityTab.entries

    Column(Modifier.fillMaxSize()) {
        // 탭바
        TabRow(selectedTabIndex = tabs.indexOf(tab)) {
            tabs.forEach { t ->
                Tab(
                    selected = (t == tab),
                    onClick = { if (t != tab) onSelectTab(t) },
                    text = { Text(t.label) }
                )
            }
        }

        // 탭별 더미 데이터
        val posts = remember(tab) { samplePosts(tab) }

        // 목록
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                PostItem(
                    post = post,
                    onClick = { onOpenPost(post.id) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// --- UI 구성 요소 ---
private data class Post(
    val id: String,
    val title: String,
    val author: String,
    val like: Int,
    val comment: Int,
    val thumbnailRes: Int? = null
)

@Composable
private fun PostItem(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (post.thumbnailRes != null) {
                Image(
                    painter = painterResource(id = post.thumbnailRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(end = 12.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📝")
                }
            }

            Column(Modifier.weight(1f)) {
                Text(
                    post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${post.author} · 👍 ${post.like} · 💬 ${post.comment}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// --- 샘플 데이터 ---
private fun samplePosts(tab: CommunityTab): List<Post> = when (tab) {
    CommunityTab.Feed -> List(10) {
        Post(
            id = "feed-$it",
            title = "오늘 하늘 미쳤다 ${it + 1}",
            author = "user$it",
            like = (5..60).random(),
            comment = (0..12).random(),
            thumbnailRes = null
        )
    }
    CommunityTab.Hot -> List(8) {
        Post(
            id = "hot-$it",
            title = "🔥 이번 주 인기 관측 포인트 ${it + 1}",
            author = "astro$it",
            like = (50..300).random(),
            comment = (10..80).random(),
            thumbnailRes = R.drawable.ic_star
        )
    }
    CommunityTab.My -> List(6) {
        Post(
            id = "my-$it",
            title = "내 기록 ${it + 1}",
            author = "나",
            like = (0..20).random(),
            comment = (0..10).random(),
            thumbnailRes = null
        )
    }
}

// --- 미리보기 ---
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun CommunityScreenPreview() {
    AppTheme {
        var current by remember { mutableStateOf(CommunityTab.Feed) }
        CommunityScreen(
            tab = current,
            onSelectTab = { current = it },
            onOpenPost = {}
        )
    }
}

