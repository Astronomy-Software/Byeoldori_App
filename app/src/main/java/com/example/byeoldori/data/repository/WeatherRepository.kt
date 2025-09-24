package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.WeatherApi
import com.example.byeoldori.data.model.dto.ForecastResponse
import com.example.byeoldori.data.model.dto.MidForecast
import com.example.byeoldori.data.model.dto.ShortForecast
import com.example.byeoldori.ui.components.observatory.midWeatherIcon
import com.example.byeoldori.ui.components.observatory.shortWeatherIcon
import com.example.byeoldori.viewmodel.Observatory.DailyForecast
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class WeatherRepository @Inject constructor( //Hilt가 WeatherApi 객체를 만들어서 자동으로 넣어줌
    private val weatherApi: WeatherApi,
) {
    suspend fun getDaily(lat: Double, lon: Double): List<DailyForecast> {
        val response = weatherApi.getForecastData(lat, lon) //좌표 기반으로 ForecastResponse JSON데이터를 가져옴

        val shortDaily = mapShortToDaily(response)
        val midDaily = mapMidToDaily(response)

        val today = LocalDate.now()
        val cutover = today.plusDays(3) //단기예보는 3일치만 보여주려고

        fun String.toDate() = parseMonthDay(this)

        // 단기가 비어있으면 그냥 중기만 사용
        val shortOnly = if (shortDaily.isEmpty()) emptyList()
        else shortDaily.filter { !it.date.toDate().isAfter(cutover) }

        val midOnly = midDaily.filter { it.date.toDate().isAfter(cutover) }

        return (shortOnly + midOnly)
            .groupBy { it.date }
            .map { (_, sameDates) -> sameDates.first() }
            .sortedBy { parseMonthDay(it.date) }
    }

    private suspend fun fetchDaily(lat: Double, lon: Double): List<DailyForecast> {

        val response = weatherApi.getForecastData(lat, lon)
        val shortDaily = mapShortToDaily(response) //단기 예보
        val midDaily = mapMidToDaily(response) //중기 예보

        val today = LocalDate.now()
        val cutover = today.plusDays(3) //오늘부터 3일 뒤
        fun String.toDate() = parseMonthDay(this)

        val shortOnly = shortDaily.filter { it.date.toDate().isBefore(cutover) }
        val midOnly = midDaily.filter { !it.date.toDate().isBefore(cutover) }

        return (shortOnly + midOnly)
            .groupBy { it.date } // 날짜별로 그룹화
            .map{ it.value.first() }
            .sortedBy { parseMonthDay(it.date) }
    }
    private fun geoKey(lat: Double, lon: Double): String { //좌표를 문자열 키로 변환
        return "${(lat)}_${(lon)}"
    }
}

// 날짜 포맷 정의
private val DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd")
private val TM_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
private val OUT_DATE_FMT = DateTimeFormatter.ofPattern("M.d")

private fun mapShortToDaily(response: ForecastResponse): List<DailyForecast> {
    val items: List<ShortForecast> = response.shortForecastResponse //ForecastResponse에서 단기예보 리스트만 꺼냄
    if(items.isEmpty()) return emptyList()

    //날짜별로 묶기
    val byDate: Map<LocalDate, List<ShortForecast>> =
        items.groupBy { LocalDateTime.parse(it.tmef,TM_FMT).toLocalDate() } //연-월-일 추출하고 같은 날짜끼리 묶음

    return byDate.map { (date, list) ->
        var tMax: Int? = null
        var tMin: Int? = null
        list.forEach { s ->
            if(s.tmx != null) tMax = (tMax ?: s.tmx).let { max(it, s.tmx) }
            if(s.tmn != null) tMin = (tMin ?: s.tmn).let { min(it, s.tmn) }
        }
        //혹시 null이면 리스트 안에서 최대최소를 뽑음
        if(tMax == null) tMax = list.maxOfOrNull { it.tmp }
        if(tMin == null) tMin = list.minOfOrNull { it.tmp }

        val pop = list.maxOfOrNull { it.pop } //가장 높은 강수 확률 사용

        val am = list.filter { LocalDateTime.parse(it.tmef, TM_FMT).hour in 6..11 }
            .maxByOrNull { it.pop } ?: list.first()
        val pm = list.filter { LocalDateTime.parse(it.tmef, TM_FMT).hour in 12..21 }
            .maxByOrNull { it.pop } ?: list.first()

        val amIcon = shortWeatherIcon(am.sky,am.pty, day = true)
        val pmIcon = shortWeatherIcon(pm.sky, pm.pty, day = false)

        DailyForecast(
            date = date.format(OUT_DATE_FMT),
            precipitation = "${pop ?: 0}%",
            amIcon = amIcon,
            pmIcon = pmIcon,
            dayTemp = "${tMax ?: 0}°",
            nightTemp = "${tMin ?: 0}°",
            suitability = "-" //나중에
        )
    }.sortedBy { it.date }
}


//중기 데이터(12시간 간격)
fun mapMidToDaily(response: ForecastResponse): List<DailyForecast> {
    val items = response.midCombinedForecastDTO
    if(items.isEmpty()) return emptyList()

    fun MidForecast.toLocalDate(): LocalDate {
        return LocalDate.parse(tmEf.substring(0, 8), DATE_FMT) //12시간 예보를 하루 단위로 묶을 준비
    }

    return items.groupBy { it.toLocalDate() }
        .map{ (date, list) -> //날짜별로 예보가 2개 있으니까 그룹화
            val tMax = list.maxOfOrNull { it.max }
            val tMin = list.minOfOrNull { it.min }
            val rnSt = list.maxOfOrNull { it.rnSt }

            //가장 많이 등작한 값 선택
            val skyCode = list.groupBy { it.sky }.maxByOrNull { it.value.size }?.key ?: "WB04"
            val preCode = list.groupBy { it.pre }.maxByOrNull { it.value.size }?.key ?: "WB00"

            val amIcon = midWeatherIcon(skyCode, preCode, day = true)
            val pmIcon = midWeatherIcon(skyCode, preCode, day = false)

            DailyForecast(
                date = date.format(OUT_DATE_FMT),
                precipitation = "${rnSt ?: 0}%",
                amIcon = amIcon,
                pmIcon = pmIcon,
                dayTemp = "${tMax ?: 0}°",
                nightTemp = "${tMin ?: 0}°",
                suitability = "-" // 나중에
            )
        }
        .sortedBy { it.date } //모든 날짜별 데이터를 리스트로 변환
}

//M.d 문자열을 LocalDate로 변환
private fun parseMonthDay(dateStr: String): LocalDate {
    val parts = dateStr.split(".")
    val month = parts[0].trim().toInt()
    val day = parts[1].trim().toInt()
    val year = LocalDate.now().year  // 올해 기준
    return LocalDate.of(year, month, day)
}

