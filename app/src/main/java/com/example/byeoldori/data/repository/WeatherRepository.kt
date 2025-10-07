package com.example.byeoldori.data.repository

import android.util.Log
import com.example.byeoldori.data.api.WeatherApi
import com.example.byeoldori.data.model.dto.*
import com.example.byeoldori.domain.Observatory.CurrentWeather
import com.example.byeoldori.domain.Observatory.DailyForecast
import com.example.byeoldori.domain.Observatory.HourlyForecast
import com.example.byeoldori.ui.components.observatory.*
import java.time.*
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

private const val TAG_REPO = "WeatherRepo"

class WeatherRepository @Inject constructor( //Hilt가 WeatherApi 객체를 만들어서 자동으로 넣어줌
    private val weatherApi: WeatherApi,
) {
    suspend fun getDaily(lat: Double, lon: Double): List<DailyForecast> {
        val response = weatherApi.getForecastData(lat, lon)
        return mapMidToDaily(response)
    }

    suspend fun getHourly(lat: Double, lon: Double): List<HourlyForecast> {
        val response = weatherApi.getForecastData(lat, lon)
        val shortHourly = mapShortHourly(response)

        fun HourlyForecast.toDateTime(): LocalDateTime {
            val (m, d) = date.split(".").map { it.trim().toInt() }
            val h = time.split("시")[0].trim().toInt()
            return LocalDateTime.of(LocalDate.now().year,m, d, h, 0, 0)
        }
        val now  = LocalDateTime.now()

        return shortHourly
            .filter{ it.toDateTime().isAfter(now) }
            .sortedBy { it.toDateTime() }
    }

    suspend fun getCurrent(lat: Double, lon: Double): CurrentWeather? =
        runCatching {
            Log.d(TAG_REPO, "GET /weather/ForecastData?lat=$lat&long=$lon")
            val res = weatherApi.getForecastData(lat, lon)
            Log.d(TAG_REPO, "Response sizes: ultra=${res.ultraForecastResponse.size}, short=${res.shortForecastResponse.size}, mid=${res.midCombinedForecastDTO.size}")
            val first = res.ultraForecastResponse.firstOrNull()
            Log.d(TAG_REPO, "ultra.first = $first")
            first?.toCurrentWeather()
        }
            .onFailure { e ->
                if (e is CancellationException) throw e
                Log.e(TAG_REPO, "getCurrent failed: ${e.message}", e)
            }
            .getOrNull()
}


// 날짜 포맷 정의
private val DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd")
private val TM_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
private val OUT_DATE_FMT = DateTimeFormatter.ofPattern("M.d")
private val HOUR_OUT_FMT = DateTimeFormatter.ofPattern("H시")


//초단기 데이터
private fun mapUltraHourly(response: ForecastResponse): List<HourlyForecast> {
    val items = response.ultraForecastResponse
    if(items.isEmpty()) return emptyList()

    return items.map { u ->
        val date = LocalDateTime.parse(u.tmef, TM_FMT)
        HourlyForecast(
            date = date.toLocalDate().format(OUT_DATE_FMT),
            time = date.format(HOUR_OUT_FMT),
            temperature = "${u.t1h}°",
            iconName = shortWeatherIcon(u.sky ?: 1, u.pty ?: 0,day = true),
            precipitation = "${u.rn1}mm",
            suitability = "-" //나중에
        )
    }.sortedBy { it.time }
}

//현재 위치에 대한 날씨 데이터(초단기 사용)
private fun UltraForecast.toCurrentWeather(): CurrentWeather = CurrentWeather(
    temperature = "${t1h}°",
    humidity = "${reh}%",
    windSpeed = "${wsd?.roundToInt()} m/s",
    suitability ="-",
    windDirection = Math.floorMod((vec ?: 0), 360)
)

//단기 데이터(1시간씩-> 3시간마다 업데이트)
private fun mapShortHourly(response: ForecastResponse): List<HourlyForecast> {
    val items = response.shortForecastResponse
    if(items.isEmpty()) return emptyList()

    return items.map { s ->
        val date = LocalDateTime.parse(s.tmef, TM_FMT)
        val isDay = date.hour in 6..18
        HourlyForecast(
            date = date.toLocalDate().format(OUT_DATE_FMT),
            time = date.format(HOUR_OUT_FMT),
            temperature = "${s.tmp}°",
            iconName = shortWeatherIcon(s.sky ?: 0, s.pty ?: 0, day = isDay),
            precipitation = "${s.pop}%",
            suitability = "-" //나중에
        )
    }.sortedBy { it.time }
}

//중기 데이터(12시간 간격)
fun mapMidToDaily(response: ForecastResponse): List<DailyForecast> {
    val items = response.midCombinedForecastDTO
    if(items.isEmpty()) return emptyList()

    fun String.toLocalDate() = LocalDate.parse(substring(0, 8), DATE_FMT)
    fun String.hour() = substring(8, 10).toInt()

    return items
        .groupBy { it.tmEf!!.toLocalDate() } // 날짜별로 묶기(오전/오후 2건)
        .map { (date, list) ->
            // 오전/오후 분리 (없을 수도 있으므로 fallback 준비)
            val amItem = list.firstOrNull { it.tmEf!!.hour() < 12 } ?: list.minBy { it.tmEf!! }
            val pmItem = list.firstOrNull { it.tmEf!!.hour() >= 12 } ?: list.maxBy { it.tmEf!! }

            // 아이콘: 오전/오후 각각의 sky/pre로 따로 계산
            val amIcon = midWeatherIcon(amItem.sky ?: "", amItem.pre ?: "", day = true)
            val pmIcon = midWeatherIcon(pmItem.sky ?: "", pmItem.pre ?: "", day = false)

            //하루 범위에서 최댓/최솟 사용
            val tMax = list.mapNotNull { it.max }.maxOrNull()
            val tMin = list.mapNotNull { it.min }.minOrNull()
            val rnSt = list.maxOfOrNull { it.rnSt ?: 0 } //습도도 높은 거 기준으로

        DailyForecast(
                date = date.format(OUT_DATE_FMT),
                precipitation = "${rnSt ?: ""}%",
                amIcon = amIcon,
                pmIcon = pmIcon,
                dayTemp = "${tMax ?: ""}°",
                nightTemp = "${tMin ?: ""}°",
                suitability = "-" // 나중에
            )
        }
        .sortedBy { parseMonthDay(it.date) } //모든 날짜별 데이터를 리스트로 변환
}

//M.d 문자열을 LocalDate로 변환
private fun parseMonthDay(dateStr: String): LocalDate {
    val parts = dateStr.split(".")
    val month = parts[0].trim().toInt()
    val day = parts[1].trim().toInt()
    val year = LocalDate.now().year  // 올해 기준
    return LocalDate.of(year, month, day)
}