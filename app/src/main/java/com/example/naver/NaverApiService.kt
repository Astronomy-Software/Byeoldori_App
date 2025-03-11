package com.example.naver

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// 네이버 API 응답 모델
data class PoiResponse(
    val places: List<PoiItem>
)

data class PoiItem(
    val name: String,
    val road_address: String?,
    val x: Double, // 경도
    val y: Double  // 위도
)

// Retrofit 인터페이스
interface NaverApiService {
    @Headers(
        "X-NCP-APIGW-API-KEY-ID: Client_ID",
        "X-NCP-APIGW-API-KEY: Client_Secret"
    )
    @GET("map-place/v1/search")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("coordinate") coordinate: String = "127.0,37.0" // 서울 기준 좌표 (변경 가능)
    ): PoiResponse

    companion object {
        fun create(): NaverApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(NaverApiService::class.java)
        }
    }
}
