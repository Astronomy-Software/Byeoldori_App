package com.example.byeoldori

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.naver.maps.map.NaverMapSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// 앱 시작전, 환경설정들을 담당하는부분
@HiltAndroidApp
class MyApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        
        // NaverMapSDK 키 설정하기
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NcpKeyClient(BuildConfig.NAVER_CLIENT_ID)
    }
    @Inject
    lateinit var imageLoaderFromHilt: ImageLoader //Coil의 기본 설정을 앱 전체에서 동일하게 쓰도록 연결

    override fun newImageLoader(): ImageLoader = imageLoaderFromHilt  //Coil이 이미지를 로드할 때 기본적으로 사용할 전역 ImageLoader 반환
}