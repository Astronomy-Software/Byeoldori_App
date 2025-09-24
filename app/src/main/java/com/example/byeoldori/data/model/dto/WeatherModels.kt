package com.example.byeoldori.data.model.dto

data class ForecastResponse(
    val ultraForecastResponse: List<UltraForecast>,
    val shortForecastResponse: List<ShortForecast>,
    val midCombinedForecastDTO: List<MidForecast>
)

data class UltraForecast(
    val tmef: String,
    val t1h: Int,
    val vec: Int, //풍향
    val wsd: Double, //풍속
    val pty: Int, //강수 형태
    val rn1: Double, //1시간 강수량
    val reh: Int, //상대습도
    val sky: Int //하늘 상태
)

data class ShortForecast(
    val tmef: String,
    val tmp: Int,
    val tmx: Int?,
    val tmn: Int?,
    val vec: Double?,
    val wsd: Double?,
    val sky: Int,
    val pty: Int,
    val pop: Int, //강수 확률
    val pcp: Double,
    val sno: Double,
    val reh: Int,
)

data class MidForecast(
    val tmFc: String,
    val tmEf: String,
    val doRegId: String,
    val siRegId: String,
    val sky: String,
    val pre: String,
    val rnSt: Int,
    val min: Int,
    val max: Int
)