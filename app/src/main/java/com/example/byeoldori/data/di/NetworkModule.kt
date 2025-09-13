package com.example.byeoldori.data.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.byeoldori.BuildConfig
import com.example.byeoldori.data.api.ApiService
import com.example.byeoldori.data.local.datastore.TokenDataStore
import com.example.byeoldori.data.remote.interceptor.AuthInterceptor
import com.example.byeoldori.data.remote.interceptor.NetworkCacheInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideCache(@ApplicationContext ctx: Context): okhttp3.Cache =
        okhttp3.Cache(File(ctx.cacheDir, "http_cache"), 20L * 1024 * 1024)

    @Provides @Singleton
    fun provideIsOnline(@ApplicationContext ctx: Context): () -> Boolean = {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork
        val nc = cm.getNetworkCapabilities(net)
        net != null && nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @Provides @Singleton
    fun provideCacheInterceptor(isOnline: () -> Boolean) =
        NetworkCacheInterceptor(isOnline = isOnline)

    @Provides @Singleton
    fun provideAuthInterceptor(tokenStore: TokenDataStore) =
        AuthInterceptor(tokenStore)

    @Provides @Singleton
    fun provideOkHttp(
        cache: okhttp3.Cache,
        auth: AuthInterceptor,
        cacheInterceptor: NetworkCacheInterceptor
    ): okhttp3.OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
        }
        return okhttp3.OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(auth)
            .addInterceptor(cacheInterceptor)
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides @Singleton
    fun provideMoshi(): com.squareup.moshi.Moshi =
        com.squareup.moshi.Moshi.Builder().build()

    @Provides @Singleton
    fun provideRetrofit(
        client: okhttp3.OkHttpClient,
        moshi: com.squareup.moshi.Moshi
    ): retrofit2.Retrofit =
        retrofit2.Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides @Singleton
    fun provideApi(retrofit: retrofit2.Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
