package com.example.byeoldori.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.byeoldori.ui.components.NavBar
import com.example.byeoldori.ui.screen.Community.CommunityScreen
import com.example.byeoldori.ui.screen.Community.CommunityTab
import com.example.byeoldori.ui.screen.Home.HomeScreen
import com.example.byeoldori.ui.screen.MyPage.MyPageScreen
import com.example.byeoldori.ui.screen.Observatory.ObservatoryScreen
import com.example.byeoldori.ui.screen.SkyMap.SkyMapScreen
import com.example.byeoldori.ui.theme.BackgroundScaffold

sealed class Root(val route: String, val label: String) {
    data object Home        : Root("home", "홈")
    data object StarMap     : Root("starmap", "별지도")
    data object Observatory : Root("observatory", "관측지")
    data object Community   : Root("community", "커뮤니티")
    data object MyPage      : Root("mypage", "마이페이지")
    companion object { val items = listOf(StarMap, Observatory, Home, Community, MyPage) }
}

@Composable
fun MainRoot(onSignOut: () -> Unit) {
    val nav = rememberNavController()

    BackgroundScaffold(
        bottomBar = { NavBar(nav) }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Root.Home.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(Root.Home.route)        { HomeScreen() }
            composable(Root.StarMap.route)     { SkyMapScreen() }
            composable(Root.Observatory.route) { ObservatoryScreen() }
            navigation(startDestination = "community/feed", route = Root.Community.route) {
                composable("community/feed") { CommunityScreen(tab = CommunityTab.Feed, onSelectTab = { t -> nav.navigate("community/$t") }) }
                composable("community/hot")  { CommunityScreen(tab = CommunityTab.Hot,  onSelectTab = { t -> nav.navigate("community/$t") }) }
                composable("community/my")   { CommunityScreen(tab = CommunityTab.My,   onSelectTab = { t -> nav.navigate("community/$t") }) }
            }
            composable(Root.MyPage.route)      { MyPageScreen() }
        }
    }
}