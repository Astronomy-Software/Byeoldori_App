package com.example.byeoldori.ui.components.community

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun Any.toShortDate(): String {
    return try {
        when (this) {
            is Long -> {
                val str = this.toString()
                if (str.length == 12) {
                    // yyyyMMddHHmm 형식 (예: 202510290000)
                    val sdf = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
                    val date = sdf.parse(str)!!
                    SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(date)
                } else {
                    // epoch millis
                    val date = Date(this)
                    SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(date)
                }
            }
            is String -> {
                when {
                    // ISO_LOCAL_DATE_TIME: 2025-10-03T15:55:00.89379
                    this.contains('T') -> {
                        val ldt = LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
                        ldt.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
                    }
                    // yyyy-MM-dd
                    this.count { it == '-' } == 2 -> {
                        val parts = this.split("-")
                        "%02d.%02d.%02d".format(
                            parts[0].takeLast(2).toInt(),
                            parts[1].toInt(),
                            parts[2].toInt()
                        )
                    }
                    // yyyyMMdd
                    this.length == 8 && this.all { it.isDigit() } -> {
                        "${this.substring(2,4)}.${this.substring(4,6)}.${this.substring(6,8)}"
                    }
                    else -> this
                }
            }
            else -> this.toString()
        }
    } catch (e: Exception) {
        this.toString()
    }
}