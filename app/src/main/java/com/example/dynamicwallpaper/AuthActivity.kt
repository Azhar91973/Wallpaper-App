package com.example.dynamicwallpaper

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.dynamicwallpaper.Common.SharedPrefs
import com.example.dynamicwallpaper.Utils.ThemeManager
import com.example.dynamicwallpaper.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var prefs: SharedPrefs

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        setUpViews()
        applySavedTheme()
        onBackPressedDispatcher.addCallback(
            this@AuthActivity, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    // TODO  handle back press properly
                    if (navController.previousBackStackEntry != null) navController.navigateUp()
                    else finish()
                }

            })
    }

    private fun setUpViews() {
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragment_auth_container) as NavHostFragment
        navController = navHost.navController
    }


    fun applySavedTheme() {
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

}