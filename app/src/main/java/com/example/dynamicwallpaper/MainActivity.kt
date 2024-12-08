package com.example.dynamicwallpaper

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.dynamicwallpaper.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

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
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navController.popBackStack()
                    navController.navigate(R.id.HomeFragment)
                    true
                }

                R.id.nav_search -> {
                    navController.popBackStack()
                    navController.navigate(R.id.searchFragment)
                    true
                }

                R.id.nav_favourite -> {
                    navController.popBackStack()
                    navController.navigate(R.id.favouriteFragment)
                    true
                }

                R.id.nav_setting -> {
                    navController.popBackStack()
                    navController.navigate(R.id.settingFragment)
                    true
                }

                else -> false
            }
        }

        // Handle back press behavior
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id != R.id.HomeFragment) {
                    navController.popBackStack()
                    navController.navigate(R.id.HomeFragment)
                    binding.bottomBar.selectedItemId = R.id.nav_home
                } else {
                    finish()
                }
            }
        })
    }
}
