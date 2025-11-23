package com.example.dynamicwallpaper.Models

data class WallpaperItems(
    val total_results: Int,
    val photos: List<Photo>
)

data class Photo(
    val id: Int,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String,
    val photographer_url: String,
    val src: PhotoSrc
)

data class PhotoSrc(
    val original: String,
    val large2x:String,
    val large:String,
    val medium: String,
    val small: String,
    val portrait:String,
    val landscape:String,
    val tiny:String
)
