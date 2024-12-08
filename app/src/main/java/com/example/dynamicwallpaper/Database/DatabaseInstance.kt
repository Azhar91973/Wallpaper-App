package com.example.dynamicwallpaper.Database
import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [FavouriteImageDataBase::class], version = 1, exportSchema = false)
abstract class DatabaseInstance : RoomDatabase() {
    abstract fun favouriteImageDao(): Dao
}
