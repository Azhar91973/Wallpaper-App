package com.example.dynamicwallpaper

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.dynamicwallpaper.Database.Dao
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.Network.WallpaperApi
import com.example.dynamicwallpaper.Paging.WallpaperPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WallpaperRepository @Inject constructor(
    private val wallpaperApi: WallpaperApi, private val favImageDao: Dao
) {

    /**
     * Fetch wallpapers with optional type and query parameters using a PagingSource.
     */
    fun getWallpapers(type: String? = null, query: String? = null): Flow<PagingData<Photo>> {
        return Pager(config = PagingConfig(
            pageSize = 80,
            maxSize = 480,
            enablePlaceholders = false // Disable placeholders for better user experience
        ), pagingSourceFactory = {
            WallpaperPagingSource(wallpaperApi, type, query)
        }).flow // Use Flow instead of LiveData for better coroutine support
    }

    /**
     * Insert a favorite image into the database.
     */
    suspend fun insertFavImage(imgUrl: FavouriteImageDataBase) {
        favImageDao.insertFavImage(imgUrl)
    }

    /**
     * Delete a favorite image from the database.
     */
    suspend fun deleteFavImage(imgUrl: FavouriteImageDataBase) {
        favImageDao.deleteFavImage(imgUrl)
    }

    /**
     * Mark images as selected based on a list of IDs.
     */
    suspend fun markSelectedImages(ids: List<Int>) {
        favImageDao.markSelectedImages(ids)
    }

    /**
     * Reset selected wallpapers in the database.
     */
    suspend fun resetSelectedWallpapers() {
        favImageDao.resetSelectedWallpapers()
    }

    /**
     * Get a list of selected images from the database.
     */
    suspend fun getSelectedImages(): List<FavouriteImageDataBase> {
        return favImageDao.getSelectedImages()
    }

    /**
     * Check if an image is present in the favorites by its URL.
     */
    suspend fun getImageByUrl(imageUrl: String): Boolean {
        return favImageDao.getImageByUrl(imageUrl) > 0
    }

    /**
     * Fetch all favorite images from the database.
     */
    suspend fun getAllFavImages(): List<FavouriteImageDataBase> {
        return favImageDao.getAllFavImages()
    }
}