package com.example.dynamicwallpaper.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Common.SharedPrefs
import com.example.dynamicwallpaper.Common.SharedPrefs.Companion.THEME_DARK
import com.example.dynamicwallpaper.Common.SharedPrefs.Companion.THEME_LIGHT
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.Utils.ThemeManager
import com.example.dynamicwallpaper.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    @Inject
    lateinit var prefs: SharedPrefs
    private val appTheme get() = prefs.getThemePreference()

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?,
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {
        if (appTheme == THEME_LIGHT) binding.themeSwitch.isChecked = false
        else if (appTheme == THEME_DARK) binding.themeSwitch.isChecked = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setUpClickListeners() {
        binding.themeSwitch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE) return@setOnTouchListener true
            false
        }
        binding.themeSwitch.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                false -> THEME_LIGHT
                true -> THEME_DARK
            }
            // Save theme preference and apply it
            prefs.saveThemePreference(themeMode)
            ThemeManager.applyTheme(themeMode, requireActivity())
        }
        binding.imgBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun setUpObservers() {
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setBottomNavigationVisibility(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as MainActivity).setBottomNavigationVisibility(true)
    }
}