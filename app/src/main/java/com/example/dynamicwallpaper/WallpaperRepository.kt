package com.example.dynamicwallpaper


import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.example.dynamicwallpaper.Database.Dao
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.Network.WallpaperApi
import com.example.dynamicwallpaper.Paging.WallpaperPagingSource
import javax.inject.Inject


class WallpaperRepository @Inject constructor(
    private val wallpaperApi: WallpaperApi, private val favImageDao: Dao
) {

    fun getWallpapers(type: String? = null, query: String? = null) =
        Pager(config = PagingConfig(pageSize = 80, maxSize = 480),
            pagingSourceFactory = { WallpaperPagingSource(wallpaperApi, type, query) }).liveData

    suspend fun insertFavImage(imgUrl: FavouriteImageDataBase) {
        favImageDao.insertFavImage(imgUrl)

    }

    suspend fun getAllFavImages(): List<FavouriteImageDataBase> {
        return favImageDao.getAllFavImages()
    }
}