package com.example.naver

import com.google.gson.annotations.SerializedName

data class GeocodeResponse(
    @SerializedName("addresses") val addresses: List<AddressItem>
)

data class AddressItem(
    @SerializedName("roadAddress") val roadAddress: String?,
    @SerializedName("x") val x: String, //경도(Longtitude)
    @SerializedName("y") val y: String  //위도(Latitude)
)
