package com.example.byeoldori.viewmodel.Observatory

import android.location.Geocoder
import android.util.Log
import com.example.byeoldori.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

fun getAddressFromLatLng(  //위도/경도 -> 주소(문자열)
    geocoder: Geocoder,
    lat: Double,
    lng: Double
): String {

    val addresses = geocoder.getFromLocation(lat, lng, 1)
    val address = addresses?.firstOrNull() //리스트에서 첫번째 주소를 가져옴
    val fullAddress = address?.getAddressLine(0) ?: "주소를 찾을 수 없습니다."
    val featureName = address?.featureName ?: ""

    return if (featureName.isNotBlank() && !fullAddress.contains(featureName)) {
        "$fullAddress ($featureName)"
    } else {
        fullAddress
    }
}

private const val TAG_GEO = "Geocoding"

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


private interface NaverReverseApi {
    @GET("map-reversegeocode/v2/gc")
    suspend fun reverse(
        @Header("X-NCP-APIGW-API-KEY-ID") id: String,
        @Header("X-NCP-APIGW-API-KEY")    secret: String,
        @Query("coords") coords: String,           // "lon,lat"
        @Query("orders") orders: String = "roadaddr,addr",
        @Query("output") output: String = "json"
    ): NaverReverseRes
}

private val naverApi by lazy {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
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

suspend fun reverseAddressRoadNaver(lat: Double, lon: Double): String = try {
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