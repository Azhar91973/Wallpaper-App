package com.example.dynamicwallpaper

import com.example.dynamicwallpaper.Database.Dao
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.Models.WallpaperItems
import com.example.dynamicwallpaper.Network.WallpaperApi
import retrofit2.Response
import javax.inject.Inject


class WallpaperRepository @Inject constructor(
    private val wallpaperApi: WallpaperApi, private val favImageDao: Dao
) {
    suspend fun getWallpapers(page: Int, query: String?, type: String): Response<WallpaperItems> {
        return wallpaperApi.getWallpaper(type, query, page)
    }

    suspend fun insertFavImage(imgUrl: FavouriteImageDataBase) {
        favImageDao.insertFavImage(imgUrl)

    }

    suspend fun getAllFavImages(): List<FavouriteImageDataBase> {
        return favImageDao.getAllFavImages()
    }
}