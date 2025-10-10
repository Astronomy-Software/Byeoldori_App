package com.example.byeoldori.data.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class NaverReverseRes(
    val results: List<Result>
){
    @JsonClass(generateAdapter = true)
    data class Result(
        val name: String,
        val region: Region? = null,
        val land: Land? = null
    ) {
        @JsonClass(generateAdapter = true)
        data class Region(
            val area1: Area? = null,
            val area2: Area? = null,
            val area3: Area? = null,
        ) {
            @JsonClass(generateAdapter = true)
            data class Area(val name: String? = null) }

        @JsonClass(generateAdapter = true)
        data class Land(
            val name: String? = null,
            val number1: String? = null,
            val number2: String? = null,
            val addition0: Addition? = null
        )  {
            @JsonClass(generateAdapter = true)
            data class Addition(val value: String? = null)
        }
    }
}

interface NaverReverseApi {
    @GET("map-reversegeocode/v2/gc")
    suspend fun reverse(
        @Header("X-NCP-APIGW-API-KEY-ID") id: String,
        @Header("X-NCP-APIGW-API-KEY")    secret: String,
        @Query("coords") coords: String,           // "lon,lat"
        @Query("orders") orders: String = "roadaddr,addr",
        @Query("output") output: String = "json"
    ): NaverReverseRes
}
