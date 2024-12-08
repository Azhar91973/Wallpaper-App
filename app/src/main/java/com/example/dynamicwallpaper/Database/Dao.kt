package com.example.dynamicwallpaper.Database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface Dao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavImage(imgUrl: FavouriteImageDataBase)

    @Query("SELECT * FROM favourite_image_DataBase ORDER BY id ASC")
    suspend fun getAllFavImages(): List<FavouriteImageDataBase>
}