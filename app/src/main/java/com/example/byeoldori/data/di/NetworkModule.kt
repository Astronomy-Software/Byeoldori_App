// data/di/NetworkModule.kt
package com.example.byeoldori.data.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.byeoldori.BuildConfig
import com.example.byeoldori.data.api.AuthApi
import com.example.byeoldori.data.api.ObservationSiteApi
import com.example.byeoldori.data.api.RefreshApi
import com.example.byeoldori.data.api.UserApi
import com.example.byeoldori.data.api.WeatherApi
import com.example.byeoldori.data.local.datastore.TokenDataStore
import com.example.byeoldori.data.remote.interceptor.AuthInterceptor
import com.example.byeoldori.data.remote.interceptor.NetworkCacheInterceptor
import com.example.byeoldori.data.remote.interceptor.RefreshTokenAuthenticator
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

// ──────────────────────────────
// Qualifiers (Retrofit/OkHttp 구분용)
// ──────────────────────────────
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthedOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthedRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ───────── 공통 제공 ─────────
    @Provides @Singleton
    fun provideCache(@ApplicationContext ctx: Context): Cache =
        Cache(File(ctx.cacheDir, "http_cache"), 20L * 1024 * 1024)

    @Provides @Singleton
    fun provideIsOnline(@ApplicationContext ctx: Context): () -> Boolean = {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork
        val nc = cm.getNetworkCapabilities(net)
        net != null && nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @Provides @Singleton
    fun provideCacheInterceptor(isOnline: () -> Boolean) =
        NetworkCacheInterceptor(isOnline)

    @Provides @Singleton
    fun provideAuthInterceptor(tokenStore: TokenDataStore) =
        AuthInterceptor(tokenStore)

    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
//            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
//            else HttpLoggingInterceptor.Level.BASIC
        }

    // ───────── (1) 비인증용 Unauth ─────────
    @Provides @Singleton @UnauthOkHttp
    fun provideUnauthOkHttp(
        cache: Cache,
        cacheInterceptor: NetworkCacheInterceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(cacheInterceptor)
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides @Singleton @UnauthRetrofit
    fun provideUnauthRetrofit(
        @UnauthOkHttp ok: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // 반드시 "/"로 끝나야 함
            .client(ok)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides @Singleton
    fun provideAuthApi(@UnauthRetrofit retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    // ───────── (2) 재발급 전용 Refresh ─────────
    @Provides @Singleton @RefreshOkHttp
    fun provideRefreshOkHttp(
        cache: Cache,
        cacheInterceptor: NetworkCacheInterceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(cacheInterceptor)
            .addInterceptor(logging)
            // ❌ AuthInterceptor 절대 넣지 않음
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides @Singleton @RefreshRetrofit
    fun provideRefreshRetrofit(
        @RefreshOkHttp ok: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(ok)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides @Singleton
    fun provideRefreshApi(@RefreshRetrofit retrofit: Retrofit): RefreshApi =
        retrofit.create(RefreshApi::class.java)

    @Provides @Singleton
    fun provideRefreshAuthenticator(
        tokenStore: TokenDataStore,
        refreshApi: RefreshApi
    ): RefreshTokenAuthenticator =
        RefreshTokenAuthenticator(tokenStore, refreshApi)

    // ───────── (3) 인증용 Authed ─────────
    @Provides @Singleton @AuthedOkHttp
    fun provideAuthedOkHttp(
        cache: Cache,
        auth: AuthInterceptor,
        cacheInterceptor: NetworkCacheInterceptor,
        logging: HttpLoggingInterceptor,
        refreshAuthenticator: RefreshTokenAuthenticator
    ): OkHttpClient =
        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(auth)               // ✅ AccessToken 붙여줌
            .addInterceptor(cacheInterceptor)
            .addInterceptor(logging)
            .authenticator(refreshAuthenticator) // ✅ 401 시 자동 재발급
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides @Singleton @AuthedRetrofit
    fun provideAuthedRetrofit(
        @AuthedOkHttp ok: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(ok)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides @Singleton
    fun provideUserApi(@AuthedRetrofit retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides @Singleton
    fun provideObservationSiteApi(@UnauthRetrofit retrofit: Retrofit): ObservationSiteApi =
        retrofit.create(ObservationSiteApi::class.java)

    @Provides @Singleton
    fun provideWeatherApi(@UnauthRetrofit retrofit: Retrofit): WeatherApi =
        retrofit.create(WeatherApi::class.java)
}
