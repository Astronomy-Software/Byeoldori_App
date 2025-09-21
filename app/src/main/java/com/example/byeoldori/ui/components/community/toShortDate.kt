package com.example.byeoldori.ui.components.community

import java.text.SimpleDateFormat
import java.util.*

fun Any.toShortDate(): String {
    return try {
        when (this) {
            is Long -> {
                val str = this.toString()
                if (str.length == 12) {
                    // yyyyMMddHHmm 형식 (예: 202510290000)
                    val sdf = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
                    val date = sdf.parse(str)
                    SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(date!!)
                } else {
                    // epoch millis
                    val date = Date(this)
                    SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(date)
                }
            }
            is String -> {
                // yyyy-MM-dd 형식 문자열
                val parts = this.split("-")
                if (parts.size == 3) {
                    "%02d.%02d.%02d".format(
                        parts[0].takeLast(2).toInt(),
                        parts[1].toInt(),
                        parts[2].toInt()
                    )
                } else {
                    this
                }
            }
            else -> this.toString()
        }
    } catch (e: Exception) {
        this.toString()
    }
}