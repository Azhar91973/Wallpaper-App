package com.example.dynamicwallpaper

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.dynamicwallpaper.Common.SharedPrefs
import com.example.dynamicwallpaper.Utils.ThemeManager
import com.example.dynamicwallpaper.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity is the host activity for the DynamicWallpaper application.
 *
 * It is responsible for setting up:
 * - The application theme based on saved preferences and system settings.
 * - Navigation via the Bottom Bar and Navigation Drawer.
 * - Handling the back press to navigate between fragments appropriately.
 *
 * This activity is annotated with @AndroidEntryPoint for Dagger Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // ViewBinding instance to access views in the layout.
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    // Navigation controller for fragment navigation.
    private lateinit var navController: NavController

    // Holds the current navigation item position (bottom bar selected index).
    private var currentNavPosition: Int = 0

    // Shared preferences helper to store/retrieve app settings.
    @Inject
    lateinit var prefs: SharedPrefs

    // Drawer layout and navigation view for the side navigation menu.
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView

    // Companion object to hold constant keys for instance state saving.
    companion object {
        private const val NAV_POSITION_KEY = "nav_position_key"
    }

    /**
     * onCreate is the entry point of the activity.
     *
     * Here we:
     * - Apply the theme.
     * - Inflate the layout.
     * - Setup navigation components and handle back button presses.
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        // Set the default theme before creating the view hierarchy.
        setTheme(R.style.Theme_DynamicWallpaper)
        super.onCreate(savedInstanceState)

        // Inflate the layout using view binding.
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply the saved theme or default to system theme.
        applySavedTheme()

        // Setup the navigation components (bottom bar, drawer, etc.)
        setupNavigation(savedInstanceState)

        // Setup custom back press handling.
        handleBackPressed()
    }

    /**
     * Applies the saved theme preference.
     *
     * If the user preference is set to system default, determine the system theme
     * (dark or light) and save that preference. Otherwise, apply the saved theme.
     */
    private fun applySavedTheme() {
        val savedTheme = prefs.getThemePreference()
        if (savedTheme == SharedPrefs.THEME_SYSTEM_DEFAULT) {
            // Determine system theme: dark or light.
            val systemTheme =
                if (ThemeManager.isSystemDarkTheme(this)) SharedPrefs.THEME_DARK else SharedPrefs.THEME_LIGHT
            // Save the determined system theme as the current preference.
            prefs.saveThemePreference(systemTheme)
            ThemeManager.applyTheme(systemTheme, this)
        } else {
            ThemeManager.applyTheme(savedTheme, this)
        }
    }

    /**
     * Initializes navigation components including the NavController,
     * Bottom Bar navigation, and Drawer Layout.
     *
     * @param savedInstanceState Bundle containing saved instance state (if any)
     */
    private fun setupNavigation(savedInstanceState: Bundle?) {
        // Restore bottom navigation position from savedInstanceState or use default.
        currentNavPosition = savedInstanceState?.getInt(NAV_POSITION_KEY, 0) ?: 0

        // Locate the NavHostFragment and get its NavController.
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        // Set the active index on the bottom bar.
        binding.bottomBar.itemActiveIndex = currentNavPosition

        // Setup navigation for the bottom bar.
        setupBottomBarNavigation()

        // Setup the Drawer Layout (side navigation).
        setupDrawerLayout()
    }

    /**
     * Configures the bottom navigation bar.
     *
     * Sets a listener that navigates to the appropriate fragment when an item is selected.
     */
    private fun setupBottomBarNavigation() {
        // Initialize the navigation view from binding.
        navigationView = binding.navView

        // Create navigation options for fragment transitions.
        val navOptions = createNavOptions()

        // Set a listener on the bottom bar for item selection.
        binding.bottomBar.setOnItemSelectedListener { menuItem ->
            when (menuItem) {
                0 -> navigateToDestination(R.id.HomeFragment, navOptions)
                1 -> navigateToDestination(R.id.categoryFragment, navOptions)
                2 -> navigateToDestination(R.id.favouriteFragment, navOptions)
            }
        }
    }

    /**
     * Sets up the Drawer Layout.
     *
     * The drawer is locked in closed mode as it is not intended to be opened via swipe.
     */
    private fun setupDrawerLayout() {
        drawerLayout = binding.drawerLayout
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    /**
     * Handles the back press behavior.
     *
     * Uses OnBackPressedCallback to provide custom navigation logic depending on the current destination.
     */
    private fun handleBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Determine current destination and handle accordingly.
                when (navController.currentDestination?.id) {
                    R.id.HomeFragment -> {
                        // If on HomeFragment, finish the activity.
                        finish()
                    }

                    R.id.viewWallpaperFragment, R.id.searchFragment -> {
                        // Pop the back stack to return to the previous fragment.
                        navController.popBackStack()
                    }

                    else -> {
                        // For all other destinations, navigate back to HomeFragment.
                        navigateToDestination(R.id.HomeFragment)
                        binding.bottomBar.itemActiveIndex = 0
                    }
                }
            }
        })
    }

    /**
     * Creates NavOptions to configure fragment transition animations.
     *
     * @return NavOptions configured with enter and exit animations.
     */
    private fun createNavOptions(): NavOptions {
        return NavOptions.Builder().setEnterAnim(R.anim.fade_in).setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in).setPopExitAnim(R.anim.fade_out).build()
    }

    /**
     * Navigates to the specified destination.
     *
     * @param destinationId The destination fragment ID.
     * @param navOptions Optional navigation options for animation.
     */
    private fun navigateToDestination(destinationId: Int, navOptions: NavOptions? = null) {
        navController.navigate(destinationId, null, navOptions)
    }

    /**
     * Sets the visibility of the bottom navigation bar.
     *
     * @param visible True to show the bottom navigation bar, false to hide.
     */
    fun setBottomNavigationVisibility(visible: Boolean) {
        binding.bottomBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * Saves the current bottom navigation index when the activity instance is saved.
     *
     * @param outState Bundle to which the state is saved.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(NAV_POSITION_KEY, binding.bottomBar.itemActiveIndex)
    }

    /**
     * Clean up the view binding instance when the activity is destroyed to prevent memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}