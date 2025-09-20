package com.example.byeoldori.data.model.dto

data class ForecastResponse(
    val ultraForecastResponse: List<UltraForecast>,
    val shortForecastResponse: List<ShortForecast>,
    val midCombinedForecastDTO: List<MidForecast>
)

data class UltraForecast(
    val fcstTm: String,
    val tmn: Int,
    val tmx: Int,
    val wd1: Int,
    val wd2: Int,
    val reh: Int,
    val rn1: Int,
    val pty: Int,
    val sky: Int
)

data class ShortForecast(
    val fcstTm: String,
    val tmp: Int,
    val tmn: Int,
    val tmx: Int,
    val wd1: Int,
    val wd2: Int,
    val reh: Int,
    val rn1: Int,
    val pty: Int,
    val sky: Int,
    val pop: Int
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