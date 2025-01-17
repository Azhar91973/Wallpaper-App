package com.example.dynamicwallpaper

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.dynamicwallpaper.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var currentNavPosition: Int = 0

    companion object {
        private const val NAV_POSITION_KEY = "nav_position_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentNavPosition = savedInstanceState?.getInt(NAV_POSITION_KEY, 0) ?: 0
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment

        navController = navHostFragment.navController
        binding.bottomBar.itemActiveIndex = currentNavPosition
        val navOptions =
            NavOptions.Builder().setEnterAnim(R.anim.fade_in).setExitAnim(R.anim.fade_out)
                .setPopEnterAnim(R.anim.fade_in).setPopExitAnim(R.anim.fade_out).build()
        binding.bottomBar.setOnItemSelectedListener { menuItem ->
            when (menuItem) {
                0 -> navController.navigate(R.id.HomeFragment, null, navOptions)
                1 -> navController.navigate(R.id.categoryFragment, null, navOptions)
                2 -> navController.navigate(R.id.favouriteFragment, null, navOptions)
                3 -> navController.navigate(R.id.settingFragment, null, navOptions)
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (navController.currentDestination?.id) {
                    R.id.HomeFragment -> finish()
                    R.id.viewWallpaperFragment -> navController.popBackStack()
                    R.id.searchFragment -> navController.popBackStack()
                    else -> {
                        navController.popBackStack(R.id.HomeFragment, false)
                        binding.bottomBar.itemActiveIndex = 0
                    }
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(NAV_POSITION_KEY, binding.bottomBar.itemActiveIndex)
    }

    fun setBottomNavigationVisibility(visible: Boolean) {
        binding.bottomBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun getBottomNavStatus(): Boolean {
        return binding.bottomBar.visibility == View.VISIBLE
    }
}