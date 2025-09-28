@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.byeoldori.ui.screen.Observatory

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.example.byeoldori.ui.components.observatory.*
import com.example.byeoldori.ui.theme.*
import com.example.byeoldori.viewmodel.Observatory.*
import com.example.byeoldori.viewmodel.observatoryList
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.launch

private const val TAG_OBS = "ObservatoryScreen"


@Composable
fun ObservatoryScreen(
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchTrigger by rememberSaveable { mutableStateOf(0) }
    var showOverlay by rememberSaveable { mutableStateOf(false) }
    var selectedLatLng  by rememberSaveable { mutableStateOf<LatLng?>(null) }
    var selectedAddress by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedInfo by remember { mutableStateOf<MarkerInfo?>(null) } //시트에 표시할 데이터
    val onSearch: (String)->Unit = { query -> searchQuery = query}
    val listState: LazyListState = rememberLazyListState()
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
    var currentLat by rememberSaveable { mutableStateOf<Double?>(null) }
    var currentLon by rememberSaveable { mutableStateOf<Double?>(null) }

    LaunchedEffect(currentLat, currentLon, selectedInfo) {
        Log.d(TAG_OBS, "to card -> lat=$currentLat lon=$currentLon info=${selectedInfo?.name}")
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 280.dp, // TODO : 이거 길이에따라 달라지는데 그 알아서 잘맞추세요
        sheetDragHandle = { BottomSheetDefaults.DragHandle() }, //이 부분이 드래그 핸들 추가 코드
        //sheetDragHandle = null,
        sheetShape = if (sheetState.currentValue == SheetValue.Expanded) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
        sheetContainerColor = Blue800,
        // 하단 시트에 들어갈 콘텐츠
        sheetContent = {
            // 마커 정보가 있으면 ObservatoryInfoCard를 표시
            if (selectedInfo != null) {
                ObservatoryInfoCard(
                    info = selectedInfo!!,
                    listState = listState,
                    currentLat = currentLat,
                    currentLon = currentLon,
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
                    onMarkerClick = onMarkerClick,
                    onCurrentLocated = { lat, lon ->
                        currentLat = lat
                        currentLon = lon
                       //Log.d(TAG_SCREEN, "onCurrentLocated -> cur=($currentLat,$currentLon)")
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
                // TODO : 버튼 컴포넌트로 수정하기
                LightPollutionButton(
                    checked = showOverlay,
                    onCheckedChange = { showOverlay = it },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(y = (-10).dp)
                        .padding(16.dp)
                )
            }
        }
    )
}

