@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byeoldori.ui.components.NavigationBar
import com.example.byeoldori.ui.components.observatory.MarkerCardWithGradient
import com.example.byeoldori.ui.components.observatory.ObservatoryInfoCard
import com.example.byeoldori.ui.theme.Blue500
import com.example.byeoldori.ui.theme.Blue700
import com.example.byeoldori.ui.theme.Blue800
import com.example.byeoldori.ui.theme.Purple500
import com.example.byeoldori.ui.theme.Purple800
import com.example.byeoldori.ui.theme.TextHighlight
import com.example.byeoldori.viewmodel.AppScreen
import com.example.byeoldori.viewmodel.Observatory.MarkerInfo
import com.example.byeoldori.viewmodel.NavigationViewModel
import com.example.byeoldori.viewmodel.Observatory.SearchBox
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.launch


@Composable
fun ObservatoryScreen(
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchTrigger by rememberSaveable { mutableStateOf(0) }
    var showOverlay by rememberSaveable { mutableStateOf(false) }
    var selectedLatLng  by rememberSaveable { mutableStateOf<LatLng?>(null) }
    var selectedAddress by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedInfo by remember { mutableStateOf<MarkerInfo?>(null) } // ✅ 시트에 표시할 데이터
    val onSearch: (String)->Unit = { query -> searchQuery = query}
    val navViewModel: NavigationViewModel = viewModel()

    val listState: LazyListState = rememberLazyListState()

    // 하단 메뉴 선택 상태
    var selectedBottomItem by rememberSaveable { mutableStateOf("관측지") }

    //추가
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden, // 앱 시작 시 시트 숨기기
        skipHiddenState = false // 시트를 숨길 수 있도록 허용
    )
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)
    val scope = rememberCoroutineScope()

    // 마커 클릭 로직: 현재 상태에 따라 시트를 확장하거나 부분 확장
    val onMarkerClick: (MarkerInfo) -> Unit = { clickedInfo ->
        // 새로운 마커를 클릭했거나, 현재 시트가 숨겨져 있다면
        if (selectedInfo != clickedInfo || sheetState.currentValue == SheetValue.Hidden) {
            selectedInfo = clickedInfo // 새 정보로 업데이트
            scope.launch { sheetState.partialExpand() } // 250dp 높이로 시트를 엽니다.
        } else {
            // 이미 열려있는 상태에서 동일한 마커를 다시 클릭한 경우
            scope.launch {
                if (sheetState.currentValue == SheetValue.PartiallyExpanded) {
                    sheetState.expand() // 현재 250dp면 전체 화면으로 확장
                } else if (sheetState.currentValue == SheetValue.Expanded) {
                    sheetState.partialExpand() // 현재 전체 화면이면 250dp로 다시 접기
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            // ⚠️ NavigationBar를 Scaffold의 bottomBar에 배치
            NavigationBar(
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
    ) { innerPadding -> // ⚠️ innerPadding을 사용하여 안전 영역을 확보
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 350.dp,
            sheetDragHandle = { BottomSheetDefaults.DragHandle() }, //이 부분이 드래그 핸들 추가 코드
            //sheetDragHandle = null,
            sheetShape = MaterialTheme.shapes.large,
            sheetContainerColor = Blue800,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()), // ⚠️ 패딩 적용
            // 하단 시트에 들어갈 콘텐츠
            sheetContent = {
                // 마커 정보가 있으면 ObservatoryInfoCard를 표시
                if (selectedInfo != null) {
                    ObservatoryInfoCard(
                        info = selectedInfo!!,
                        listState = listState,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // 없으면 기본 텍스트를 표시
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 250.dp)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("지도의 마커를 선택하면 상세 카드가 열립니다.", color = TextHighlight)
                    }
                }
            },
            // 하단 시트 아래에 위치할 콘텐츠 (지도 및 오버레이 UI)
            content = {
                // 이 안에서 NavermapScreen을 딱 한 번만 호출
                Box(modifier = Modifier.fillMaxSize()) {
                    NavermapScreen(
                        searchQuery = searchQuery,
                        onSearch = { searchQuery = it },
                        showOverlay = showOverlay,
                        onLatLngUpdated = { selectedLatLng = it },
                        onAddressUpdated = { selectedAddress = it },
                        searchTrigger = searchTrigger,
                        onMarkerClick = onMarkerClick
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
                            containerColor = if (showOverlay) Blue500 else Purple500, // 파란색 ↔ 보라색
                            contentColor = TextHighlight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(y = (-10).dp)
                            .padding(16.dp)
                            .height(50.dp)
                    ) {
                        Text(if (showOverlay) "광공해 끄기" else "광공해 보기", fontSize = 12.sp)
                    }
                }
            }
        )
    }
}

