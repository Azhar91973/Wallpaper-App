package com.example.dynamicwallpaper.WallpaperService

import android.app.WallpaperManager
import android.content.Context
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.dynamicwallpaper.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallpaperHelper(private val context: Context) {

    fun setWallpaper(imageUrl: String, type: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = Glide.with(context).asBitmap().load(imageUrl).submit().get()
                val wallpaperManager = WallpaperManager.getInstance(context)
                when (type) {
                    HOME_SCREEN -> wallpaperManager.setBitmap(
                        bitmap, null, true, WallpaperManager.FLAG_SYSTEM
                    )

                    LOCK_SCREEN -> wallpaperManager.setBitmap(
                        bitmap, null, true, WallpaperManager.FLAG_LOCK
                    )

                    BOTH_SCREENS -> wallpaperManager.setBitmap(bitmap)
                }
                withContext(Dispatchers.Main) {
                    showToast(context.getString(R.string.wallpaper_set_successfully))
                }
            } catch (e: Exception) {
                showToast(context.getString(R.string.failed_to_set_wallpaper))
                withContext(Dispatchers.Main) {
                    showToast("${context.getString(R.string.failed_to_set_wallpaper)} :${e.localizedMessage}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Helper method to download the wallpaper image (this could be any logic you want)
    fun downloadWallpaperWithNotification() {}

    companion object {
        const val HOME_SCREEN = 0
        const val LOCK_SCREEN = 1
        const val BOTH_SCREENS = 2
    }
}