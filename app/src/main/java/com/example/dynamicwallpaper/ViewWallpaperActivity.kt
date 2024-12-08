package com.example.dynamicwallpaper

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.dynamicwallpaper.databinding.ActivityViewWallpaperBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewWallpaperActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewWallpaperBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewWallpaperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHost.navController

        val destination = when (intent.getStringExtra("destination")) {
            "ViewWallpaperFragment" -> R.id.viewWallpaperFragment
            else -> R.id.viewWallpaperFragment
        }
        val bundle = Bundle().apply {
            putString("cachedWallpaper", intent.getStringExtra("cachedWallpaper"))
            putInt("position", intent.getIntExtra("position", 0))
            putInt("page", intent.getIntExtra("page", 1))
        }
        navController.navigate(destination, bundle)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.clearBackStack(0)
                finish()
            }
        })
    }
}