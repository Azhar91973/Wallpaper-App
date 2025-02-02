package com.example.dynamicwallpaper.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavImage(imgUrl: FavouriteImageDataBase)

    // mark the images selected by using Image Id
    @Query("UPDATE FAVOURITE_IMAGE_DATABASE SET isSelected = 1 WHERE id IN (:imageIds) AND isSelected IS NULL")
    suspend fun markSelectedImages(imageIds: List<Int>)

    // reset the images marked as selected
    @Query("UPDATE FAVOURITE_IMAGE_DATABASE SET isSelected = NULL WHERE isSelected = 1")
    suspend fun resetSelectedWallpapers()

    // get all the images which are marked as selected
    @Query("SELECT * FROM FAVOURITE_IMAGE_DATABASE WHERE isSelected = 1")
    suspend fun getSelectedImages(): List<FavouriteImageDataBase>

    @Delete
    suspend fun deleteFavImage(imgUrl: FavouriteImageDataBase)

    @Query("SELECT COUNT(*) FROM FAVOURITE_IMAGE_DATABASE WHERE imageUrl = :imageUrl")
    suspend fun getImageByUrl(imageUrl: String): Int

    @Query("SELECT * FROM FAVOURITE_IMAGE_DATABASE ORDER BY id ASC")
    fun getAllFavImages(): Flow<List<FavouriteImageDataBase>>
}