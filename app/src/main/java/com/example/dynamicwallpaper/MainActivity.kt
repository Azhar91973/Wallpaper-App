package com.example.dynamicwallpaper

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.dynamicwallpaper.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import me.ibrahimsn.lib.SmoothBottomBar

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up navigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        // Handle bottom navigation item clicks
        binding.bottomBar.setOnItemSelectedListener { menuItem ->
            when (menuItem) {
                0 -> navigateToFragment(R.id.HomeFragment)
                1 -> navigateToFragment(R.id.categoryFragment)
                2 -> navigateToFragment(R.id.favouriteFragment)
                3 -> navigateToFragment(R.id.settingFragment)
            }
        }

        // Handle back press behavior
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(
                    "OnBackPress",
                    "handleOnBackPressed:${R.id.HomeFragment}   ${navController.currentDestination?.id}"
                )
                when (navController.currentDestination?.id) {
                    R.id.HomeFragment -> {
                        finish()
                    }

                    R.id.viewWallpaperFragment -> {
                        navController.popBackStack()
                    }
                    R.id.searchFragment -> {
                        navController.popBackStack()
                    }

                    else -> {
                        navController.popBackStack(R.id.HomeFragment, false)
                        binding.bottomBar.itemActiveIndex = 0
                    }
            }
        }
    })
}

fun navigateToFragment(fragmentId: Int, navPos: Int? = null) {
    if (navController.currentDestination?.id != fragmentId) {
        if (navPos != null) {
            binding.bottomBar.itemActiveIndex = navPos
        }
        navController.navigate(fragmentId) // Navigate to the selected fragment
    }
}

fun setBottomNavigationVisibility(visible: Boolean) {
    val bottomNav = findViewById<SmoothBottomBar>(R.id.bottomBar)
    bottomNav.visibility = if (visible) View.VISIBLE else View.GONE
}

fun getBottomNavStatus(): Boolean {
    return binding.bottomBar.visibility == View.VISIBLE
}
}
