package com.example.dynamicwallpaper.Network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WallpaperModule {
    @Provides
    @Singleton
    fun providesWallpaperApi(): WallpaperApi {
        return Retrofit.Builder().baseUrl("https://api.pexels.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(WallpaperApi::class.java)
    }
}