package com.example.byeoldori.ui.components.observatory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.byeoldori.R
import com.example.byeoldori.ui.theme.*
import com.naver.maps.geometry.LatLng

@Composable
fun MarkerPopup (
    selectedLatLng: LatLng?,
    selectedAddress: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latLng = selectedLatLng
    val address = selectedAddress

    AnimatedVisibility(
        visible = latLng != null && !address.isNullOrBlank(),
        modifier = modifier
    ) {
        // exit 애니메이션 중 null로 바뀌어도 그리지 않도록 방어
        if (latLng == null || address.isNullOrBlank()) return@AnimatedVisibility
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(5.dp, Purple500),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Spacer(Modifier.height(6.dp))
                Text("위도: ${"%.6f".format(selectedLatLng!!.latitude)}",
                    color = NaverGray,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Text("경도: ${"%.6f".format(selectedLatLng.longitude)}",
                    color = NaverGray,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Text("주소: ${selectedAddress!!}", color = NaverGray, maxLines = 2, fontSize = 12.sp)
                //Spacer(Modifier.height(4.dp))
                Row {
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("닫기", color = NaverGrayA) }
                }
            }
        }
    }
}

@Preview(
    name = "MarkerPopup",
    showBackground = true,
    backgroundColor = 0xFF000000
)

@Composable
private fun Preview_PopupCard() {
    MaterialTheme {
        MarkerPopup(
            selectedLatLng = LatLng(36.642123, 127.489876),
            selectedAddress = "충청북도 청주시 서원구 충대로 1",
            onDismiss = {}
       )
    }
}
