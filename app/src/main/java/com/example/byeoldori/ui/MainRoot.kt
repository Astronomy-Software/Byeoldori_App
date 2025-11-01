package com.example.byeoldori.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.byeoldori.data.UserViewModel
import com.example.byeoldori.skymap.SkyMode
import com.example.byeoldori.skymap.StellariumScreen
import com.example.byeoldori.ui.components.NavBar
import com.example.byeoldori.ui.components.mypage.LikeSection
import com.example.byeoldori.ui.screen.Community.CommunityScreen
import com.example.byeoldori.ui.screen.Community.CommunityTab
import com.example.byeoldori.ui.screen.MyPage.MyPageScreen
import com.example.byeoldori.ui.screen.Observatory.ObservatoryScreen
import com.example.byeoldori.ui.screen.home.HomeScreen
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
fun MainRoot() {
    val nav = rememberNavController()
    val userVm: UserViewModel = hiltViewModel()

    LaunchedEffect(Unit) { userVm.getMyProfile() }

    BackgroundScaffold(
        bottomBar = { NavBar(nav) }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Root.Home.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(Root.Home.route)        { HomeScreen() }
            composable(Root.StarMap.route)     { StellariumScreen(SkyMode.OBSERVATION) }
            composable(Root.Observatory.route) { ObservatoryScreen() }
            navigation(startDestination = "community/home", route = Root.Community.route) {
                composable("community/home") { CommunityScreen(tab = CommunityTab.Home, onSelectTab = { t -> nav.navigate("community/$t") }, userVm = userVm) }
                composable("community/review")  { CommunityScreen(tab = CommunityTab.Review,  onSelectTab = { t -> nav.navigate("community/$t") }, userVm = userVm) }
                composable("community/board")   { CommunityScreen(tab = CommunityTab.Board,   onSelectTab = { t -> nav.navigate("community/$t") }, userVm = userVm) }
                composable("community/program")   { CommunityScreen(tab = CommunityTab.Program,   onSelectTab = { t -> nav.navigate("community/$t") }, userVm = userVm) }
            }
            navigation(startDestination = "mypage/home", route = Root.MyPage.route) {
                composable("mypage/home") { MyPageScreen(onOpenLikes = { nav.navigate("mypage/likes") }) }
                composable("mypage/likes") { LikeSection(onBack = { nav.popBackStack() }) }
            }
        }
    }
}