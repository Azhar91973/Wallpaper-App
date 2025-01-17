package com.example.dynamicwallpaper.WallpaperService

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dynamicwallpaper.Common.SharedPrefs
import com.example.dynamicwallpaper.Common.SharedPrefs.Companion.INDEX
import com.example.dynamicwallpaper.Common.SharedPrefs.Companion.WALLPAPER_SET_TYPE
import com.example.dynamicwallpaper.WallpaperRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WallpaperWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val wallpaperRepository: WallpaperRepository // No @Assisted
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = SharedPrefs(context)
        val currentIndex = prefs.getInt(INDEX)
        val wallpaperSetType = prefs.getWallpaperSetType(WALLPAPER_SET_TYPE)
        val selectedWallpaperList = wallpaperRepository.getSelectedImages()
        Log.d("InsideWorker", "doWork: ${selectedWallpaperList.size}")
        WallpaperHelper(context).setWallpaper(
            selectedWallpaperList[currentIndex].imageUrl, wallpaperSetType
        )
        if (currentIndex + 1 < selectedWallpaperList.size) prefs.saveInt(
            INDEX, currentIndex + 1
        )
        else prefs.saveInt(INDEX, 0)
        return Result.success()
    }
}
