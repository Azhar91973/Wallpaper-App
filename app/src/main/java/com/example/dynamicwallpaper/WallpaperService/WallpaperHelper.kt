package com.example.dynamicwallpaper.WallpaperService

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

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
                    showToast("Wallpaper Set Successfully")
                }
            } catch (e: Exception) {
                showToast("Failed to set wallpaper")
                withContext(Dispatchers.Main) {
                    showToast("Failed to set wallpaper: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Helper method to download the wallpaper image (this could be any logic you want)
    suspend fun downloadWallpaperWithNotification(context: Context, imageUrl: String) {
        val notificationId = 1
        val channelId = "wallpaper_download_channel"

        // Create Notification Channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Wallpaper Downloads"
            val descriptionText = "Notifications for wallpaper downloads"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create Initial Notification with Progress
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setContentTitle("Downloading Wallpaper").setContentText("Download in progress...")
            .setPriority(NotificationCompat.PRIORITY_LOW).setOngoing(true)
            .setProgress(100, 0, true) // Indeterminate progress bar

        // Show the initial notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(notificationId, builder.build())

        try {
            // Download the wallpaper
            val bitmap = downloadWallpaper(imageUrl)

            // Update the notification to indicate success
            builder.setContentText("Download complete").setProgress(0, 0, false).setOngoing(false)
                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            // Update the notification to indicate failure
            builder.setContentText("Download failed").setProgress(0, 0, false).setOngoing(false)
            notificationManager.notify(notificationId, builder.build())
        }
    }

    suspend fun downloadWallpaper(imageUrl: String): Bitmap {
        return withContext(Dispatchers.IO) {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream = connection.inputStream
            return@withContext BitmapFactory.decodeStream(inputStream)
        }
    }


    // Helper method to set the downloaded image as wallpaper
    private fun setWallpaperBitmap(bitmap: Bitmap) {
        // Code to set the bitmap as the device's wallpaper
        val wallpaperManager = WallpaperManager.getInstance(context)
        wallpaperManager.setBitmap(bitmap)
    }

    companion object {
        const val HOME_SCREEN = 0
        const val LOCK_SCREEN = 1
        const val BOTH_SCREENS = 2
    }
}