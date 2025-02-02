package com.example.dynamicwallpaper.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dynamicwallpaper.Utils.Constants.FAVOURITE_IMAGE_DATABASE
@Entity(tableName = FAVOURITE_IMAGE_DATABASE)
data class FavouriteImageDataBase(
    @ColumnInfo(name = "imageUrl") var imageUrl: String,
    @ColumnInfo(name = "isSelected") var isSelected: Boolean? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0
}
