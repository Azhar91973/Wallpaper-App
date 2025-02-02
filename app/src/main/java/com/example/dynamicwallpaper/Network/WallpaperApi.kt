package com.example.dynamicwallpaper.Network

import com.example.dynamicwallpaper.Models.WallpaperItems
import com.example.dynamicwallpaper.Utils.ApiRoutes.SEARCH_WALLPAPERS
import com.example.dynamicwallpaper.Utils.ApiRoutes.WALLPAPERS
import com.example.dynamicwallpaper.Utils.Constants.PER_PAGE_ITEMS
import retrofit2.http.GET
import retrofit2.http.Query

interface WallpaperApi {
    @GET("v1/$WALLPAPERS")
    suspend fun getWallpaper(
        @Query("page") page: Int, @Query("per_page") perPage: Int = PER_PAGE_ITEMS
    ): WallpaperItems

    @GET("v1/$SEARCH_WALLPAPERS")
    suspend fun searchWallpaper(
        @Query("query") query: String?,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = PER_PAGE_ITEMS
    ): WallpaperItems
}