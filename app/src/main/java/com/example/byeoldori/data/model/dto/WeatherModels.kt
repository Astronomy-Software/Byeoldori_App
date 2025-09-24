package com.example.byeoldori.data.model.dto

data class ForecastResponse(
    val ultraForecastResponse: List<UltraForecast>,
    val shortForecastResponse: List<ShortForecast>,
    val midCombinedForecastDTO: List<MidForecast>
)

data class UltraForecast(
    val tmef: String,
    val t1h: Int,
    val vec: Int,
    val wsd: Double,
    val pty: Int,
    val rn1: Double,
    val reh: Int,
    val sky: Int
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
    val pop: Int,
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