package com.example.byeoldori.ui.screen.Observatory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservatoryScreen(
    onNavigateTo: () -> Unit,
) {

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchTrigger by rememberSaveable { mutableStateOf(0) }
    var showOverlay by rememberSaveable { mutableStateOf(false) }
    var selectedLatLng  by rememberSaveable { mutableStateOf<LatLng?>(null) }
    var selectedAddress by rememberSaveable { mutableStateOf<String?>(null) }
    val onSearch: (String)->Unit = { query -> searchQuery = query}

    Box(modifier = Modifier.fillMaxSize()) {
        NavermapScreen(
            onBack = onNavigateTo,
            searchQuery = searchQuery,
            onSearch = {  searchQuery = it },
            showOverlay = showOverlay,
            onLatLngUpdated   = { selectedLatLng  = it },
            onAddressUpdated  = { selectedAddress = it },
            searchTrigger     = searchTrigger
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 0.dp, start = 16.dp)  // 필요하면 위치 조정
        ) {
            SearchBox(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchClick = {
                    onSearch(searchQuery)
                    searchTrigger += 1 //검색 버튼 눌렀을 때, LaunchedEffect 반응하도록
                }

            )
            Button(
                onClick = { showOverlay = !showOverlay },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 470.dp, y = 250.dp)
                    .padding(16.dp)
            ) {
                Text(if (showOverlay) "광공해 끄기" else "광공해 보기")
            }
        }
    }
}