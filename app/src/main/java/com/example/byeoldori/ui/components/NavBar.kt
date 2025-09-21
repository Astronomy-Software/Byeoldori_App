/**
 * 최신버전 NavigationBar,
 * 이름 변경을 강력히 권장하더라구요 ...
 * 하는김에 Navigation Compose를 사용해서 크게 수정했습니다.
 * UI는 그대로인 대신에 내부로직만 바뀌었습니다.
**/
package com.example.byeoldori.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.byeoldori.R
import com.example.byeoldori.ui.Root
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.ui.theme.Purple800
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.ui.theme.TextNormal

@Composable
fun NavBar(nav: NavHostController) {
    val entry by nav.currentBackStackEntryAsState()
    val dest = entry?.destination

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Purple800)
            .padding(vertical = 5.dp)
            .navigationBarsPadding(), // 시스템 하단바 만큼 패딩 추가,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically

    ) {
        val items = listOf(
            Triple(R.drawable.ic_star, "별지도", Root.StarMap.route),
            Triple(R.drawable.ic_location_on, "관측지", Root.Observatory.route),
            Triple(R.drawable.ic_home, "홈", Root.Home.route),
            Triple(R.drawable.ic_groups, "커뮤니티", Root.Community.route),
            Triple(R.drawable.ic_account_circle, "마이페이지", Root.MyPage.route)
        )

        items.forEach { (iconRes, label, route) ->
            // 현재 선택 여부 (community/feed, community/hot 등 자식도 community로 묶임)
            val selected = dest?.hierarchy?.any { it.route == route } == true
            val bg = if (selected) Purple500 else Color.Transparent

            Box(
                modifier = Modifier
                    .clickable {
                        nav.navigate(route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    .background(bg, shape = RoundedCornerShape(10.dp))
                    .padding(horizontal = 0.dp, vertical = 2.dp)
                    .width(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        modifier = Modifier
                            .size(30.dp)
                            .offset(y = 5.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = label,
                        color = if (selected) TextHighlight else TextNormal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF120A2A,
    widthDp = 360,
    heightDp = 80
)

@Composable
private fun NavBarPreview() {
    val nav = rememberNavController()
    // 기본적으로 NavController의 currentBackStackEntry는 null이므로
    // 미리 강제로 navigate 해서 선택 상태를 확인할 수 있음
    LaunchedEffect(Unit) {
        nav.navigate(Root.Home.route)
    }
    NavBar(nav = nav)
}