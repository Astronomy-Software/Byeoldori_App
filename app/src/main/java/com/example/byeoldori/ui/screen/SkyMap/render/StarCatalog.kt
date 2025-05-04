package com.example.byeoldori.ui.screen.SkyMap.render

import kotlin.random.Random

class StarCatalog(private val manager: StarManager) {
    init {
        // 랜덤별 100개 추가 (색상·크기 랜덤)
        repeat(100) {
            val ra  = Random.nextFloat() * 360f                  // 0..360°
            val dec = Random.nextFloat() * 180f - 90f           // -90..+90°
            val r   = Random.nextFloat()        // 0..1
            val g   = Random.nextFloat()        // 0..1
            val b   = Random.nextFloat()        // 0..1
            val color = floatArrayOf(r, g, b, 1f)              // 불투명
            val size  = Random.nextFloat() * 4f + 1f           // 예: 1..5 포인트
            manager.addStarByRaDec(ra, dec, color, size)
        }
        // Polaris (북극성) 예시: 하얀색, 기본 크기
        manager.addStarByRaDec(37.95f, 89.26f)
    }
}
