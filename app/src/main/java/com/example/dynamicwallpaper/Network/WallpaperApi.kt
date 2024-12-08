package com.example.dynamicwallpaper.Network

import com.example.dynamicwallpaper.Models.WallpaperItems
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
interface WallpaperApi {
    @Headers("Authorization: Qww51glLjUqa4qniIeaeRRNZFXUcgkdYgdTxBnQTXmL3wGjnIHfs2jsX")
    @GET("v1/{type} ")
    suspend fun getWallpaper(
        @Path("type") type:String,
        @Query("query") query: String?=null,
        @Query("page") page:Int,
        @Query("per_page") perPage: Int = 75
    ): Response<WallpaperItems>
}