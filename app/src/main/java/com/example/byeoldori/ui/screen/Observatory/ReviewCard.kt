// ReviewCard.kt
package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.example.byeoldori.R

@Composable
fun ReviewSection(
    title: String,
    reviews: List<Review>,
    modifier: Modifier = Modifier
) {
    // ----- 페이징 상태 -----
    val pageSize = 4
    val pageCount = if (reviews.isEmpty()) 1 else ((reviews.size - 1) / pageSize + 1)
    var page by rememberSaveable { mutableStateOf(0) }
    if (page >= pageCount) page = pageCount - 1 // 안전 처리

    val start = page * pageSize
    val pageItems = reviews.drop(start).take(pageSize)

    Column(
        modifier = modifier
            .fillMaxWidth()
            //.padding(horizontal = 16.dp)
    ) {
        // 타이틀 + 페이지 컨트롤
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { if (page > 0) page-- }, enabled = page > 0) {
                Icon(
                    painter = painterResource(id = R.drawable.navigate_before),
                    contentDescription = "이전",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text("${page + 1} / $pageCount", color = Color.White)
            IconButton(
                onClick = { if (page < pageCount - 1) page++ },
                enabled = page < pageCount - 1
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.navigate_next),
                    contentDescription = "다음",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        //Spacer(Modifier.height(5.dp))

        // 2x2 그리드 (부모 LazyColumn 안이므로 높이는 유한값 권장)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp), // 유한 높이 필수
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 키: id가 중복될 수 있으니 인덱스를 섞어 임시로 안전하게
            itemsIndexed(pageItems, key = { idx, item -> "${item.id}#$start+$idx" }) { _, review ->
                ReviewCard(review)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF241860)),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(review.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Column(Modifier.padding(start = 15.dp)) {
                Text(
                    text = review.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.profile1),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(review.author, color = Color.White, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text("별점 : ${review.rating}", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.thumbs_up1),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${review.likeCount}", color = Color.White, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(x=(-8).dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.comment),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${review.commentCount}", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
