package com.example.dynamicwallpaper

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DynamicWallpaperApplication:Application()
{
    override fun onCreate() {
        super.onCreate()
    }
}