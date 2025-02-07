package com.example.dynamicwallpaper.Network

import android.content.Context
import com.example.dynamicwallpaper.BuildConfig
import com.example.dynamicwallpaper.Common.SharedPrefs
import com.example.dynamicwallpaper.Utils.ApiRoutes.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    //    Provides object of OkkHttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(loggingInterceptor).addInterceptor { chain ->
            val request = chain.request().newBuilder().addHeader(
                "Authorization", BuildConfig.API_KEY
            ).build()
            chain.proceed(request)
        }.build()
    }

    //    Provides object of Retrofit
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build()
    }

    //    Provides object of Wallpaper API
    @Provides
    @Singleton
    fun provideWallpaperApi(retrofit: Retrofit): WallpaperApi {
        return retrofit.create(WallpaperApi::class.java)
    }

    // Provides object of SharedPreference Manager
    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPrefs {
        return SharedPrefs(context)
    }
}