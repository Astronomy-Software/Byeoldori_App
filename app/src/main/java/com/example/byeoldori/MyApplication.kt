package com.example.byeoldori

import android.app.Application
import com.naver.maps.map.NaverMapSdk

// 앱 시작전, 환경설정들을 담당하는부분
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // NaverMapSDK 키 설정하기
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NcpKeyClient(BuildConfig.NAVER_CLIENT_ID)
    }
}
