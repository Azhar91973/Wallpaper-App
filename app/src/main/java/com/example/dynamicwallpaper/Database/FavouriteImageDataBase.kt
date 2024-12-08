package com.example.dynamicwallpaper.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "favourite_image_DataBase")
data class FavouriteImageDataBase(
    @ColumnInfo(name = "imageUrl") var imageUrl: String
) {
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0
}
