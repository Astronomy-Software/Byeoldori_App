package com.example.byeoldori.data.di

import android.content.Context
import com.example.byeoldori.data.WeatherCache
import com.example.byeoldori.data.WeatherDataCache
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherCacheModule {

    @Provides
    @Singleton
    fun provideWeatherCache(
        @ApplicationContext context: Context,
        moshi: Moshi
    ): WeatherCache {
        return WeatherDataCache(context, moshi)
    }
}