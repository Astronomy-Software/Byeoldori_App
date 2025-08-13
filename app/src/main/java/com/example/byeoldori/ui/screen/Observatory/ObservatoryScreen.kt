package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.R
import com.example.byeoldori.viewmodel.AppScreen
import com.example.byeoldori.viewmodel.NavigationViewModel
import com.naver.maps.geometry.LatLng

@Composable
fun ObservatoryScreen(
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchTrigger by rememberSaveable { mutableStateOf(0) }
    var showOverlay by rememberSaveable { mutableStateOf(false) }
    var selectedLatLng  by rememberSaveable { mutableStateOf<LatLng?>(null) }
    var selectedAddress by rememberSaveable { mutableStateOf<String?>(null) }
    val onSearch: (String)->Unit = { query -> searchQuery = query}
    var info by remember { mutableStateOf<MarkerInfo?>(null) }
    val navViewModel: NavigationViewModel = viewModel()
    //val scrollState = rememberScrollState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxCardHeight = screenHeight * 0.8f
    val minCardHeight = 250.dp
    val noRipple = remember { MutableInteractionSource() } //클릭시 화면 어두워지는 걸 방지

    val listState: LazyListState = rememberLazyListState()
    val density = LocalDensity.current
    // 내부 리스트 스크롤로 카드 높이 계산
    val dynamicHeight by remember {
        derivedStateOf {
            // 대략적인 보정: 아이템 평균 높이를 200px로 가정 (필요하면 조정)
            val approxItemPx = 250
            val px = listState.firstVisibleItemIndex * approxItemPx +
                    listState.firstVisibleItemScrollOffset
            val dp = with(density) { px.toDp() }
            // 감도 조절: /4 정도면 부드럽게
            (minCardHeight + dp / 4).coerceAtMost(maxCardHeight)
        }
    }
    // 하단 메뉴 선택 상태
    var selectedBottomItem by rememberSaveable { mutableStateOf("관측지") }


    Box(modifier = Modifier.fillMaxSize()) {
        NavermapScreen(
            searchQuery = searchQuery,
            onSearch = { searchQuery = it },
            showOverlay = showOverlay,
            onLatLngUpdated = { selectedLatLng = it },
            onAddressUpdated = { selectedAddress = it },
            searchTrigger = searchTrigger,
            onMarkerClick = { clickedInfo ->
                info = clickedInfo
            }
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 35.dp, start = 0.dp)

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                SearchBox(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchClick = {
                        onSearch(searchQuery)
                        searchTrigger += 1 //검색 버튼 눌렀을 때, LaunchedEffect 반응하도록
                    }
                )
                Spacer(modifier = Modifier.width(10.dp))

                MarkerCardWithGradient()
            }
        }
        Button(
            onClick = { showOverlay = !showOverlay },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (showOverlay) Color(0xFF3851CD) else Color(0xFF8459C9), // 파란색 ↔ 보라색
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-60).dp)
                .padding(16.dp)
                .height(50.dp)
        ) {
            Text(if (showOverlay) "광공해 끄기" else "광공해 보기", fontSize = 12.sp)
        }

        //관측지 정보 카드 추가
        info?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    //.align(Alignment.BottomCenter)
                    .zIndex(1f)
                    .padding(bottom = 70.dp)
                    //.verticalScroll(scrollState)
                    .clickable (
                        interactionSource = noRipple,
                        indication = null
                    ){
                        info = null
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(dynamicHeight)
                        .clickable (enabled = false) {}
                ) {
                    ObservatoryInfoCard(
                        info = it,
                        listState = listState
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp) // 둥근 모서리를 가릴 만큼
                    .offset(y = (-65).dp)
                    .align(Alignment.BottomCenter) // 정확히 하단에 위치
                    .zIndex(2f) // 카드보다 위에
                    .background(Color(0xFF473A9D)) // 카드와 같은 배경색
            )
        }



        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavBar(
                selectedItem = selectedBottomItem,
                onItemSelected = { item ->
                    selectedBottomItem = item
                    when (item) {
                        "홈" -> {}
                        "별지도" -> navViewModel.navigateTo(AppScreen.SkyMap)
                        "관측지" -> {}
                        "커뮤니티" -> {}
                        "마이페이지" -> navViewModel.navigateTo(AppScreen.MyPage)
                    }
                }
            )
        }


    }
}
