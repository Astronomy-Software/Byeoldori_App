package com.example.byeoldori.data.repository

import android.util.Log
import com.example.byeoldori.BuildConfig
import com.example.byeoldori.data.api.NaverReverseApi
import com.example.byeoldori.data.api.NaverReverseRes
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_GEO = "GeocodingRepo"

@Singleton
class NavermapRepository @Inject constructor(){

    val naverApi by lazy {
        val moshi = Moshi.Builder()
            .build()

        val client = OkHttpClient.Builder()
            .build()

        Retrofit.Builder()
            .baseUrl("https://maps.apigw.ntruss.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()
            .create(NaverReverseApi::class.java)
    }


    //위도경도 -> 주소
    suspend fun reverseAddressRoad(lat: Double, lon: Double): String = try {
        Log.d(TAG_GEO, ">>> reverseAddressRoadNaver start: lat=$lat, lon=$lon")

        val res = naverApi.reverse(
            id = BuildConfig.NAVER_CLIENT_ID.trim(),
            secret = BuildConfig.NAVER_CLIENT_SECRET.trim(),
            coords = "$lon,$lat",
            orders = "roadaddr,addr"
        )

        Log.d(TAG_GEO, "Naver API call success, results.size=${res.results.size}")
        res.results.forEachIndexed { idx, r ->
            Log.d(TAG_GEO, "Result[$idx]: name=${r.name}, region=${r.region}, land=${r.land}")
        }

        val road  = res.results.firstOrNull { it.name == "roadaddr" }
        val jibun = res.results.firstOrNull { it.name == "addr" }

        Log.d(TAG_GEO, "road=$road, jibun=$jibun")

        fun build(r: NaverReverseRes.Result?): String? {
            r ?: return null
            val reg = r.region
            val addr = buildString {
                append(reg?.area1?.name.orEmpty())
                if (!reg?.area2?.name.isNullOrBlank()) append(" ${reg?.area2?.name}")
                if (!reg?.area3?.name.isNullOrBlank()) append(" ${reg?.area3?.name}")
                r.land?.let { land ->
                    if (!land.name.isNullOrBlank()) append(" ${land.name}")
                    if (!land.number1.isNullOrBlank()) append(" ${land.number1}")
                    if (!land.number2.isNullOrBlank()) append("-${land.number2}")
                    val b = land.addition0?.value
                    if (!b.isNullOrBlank()) append(" ($b)")
                }
            }.ifBlank { null }

            Log.d(TAG_GEO, "build($r.name) -> $addr")
            return addr
        }

        val built = build(road) ?: build(jibun) ?: ""
        Log.d(TAG_GEO, "Final address='$built'")

        built
    } catch (e: Exception) {
        Log.e(TAG_GEO, "reverseAddressRoadNaver failed: ${e.message}", e)
        ""
    }
}