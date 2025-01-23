package com.example.dynamicwallpaper.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.findNavController
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Common.SharedPrefs
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.databinding.FragmentSettingBinding


class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    private lateinit var prefs: SharedPrefs
    private val isDarkMode get() = prefs.isDarkMode()

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = SharedPrefs(requireContext())
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {
        binding.themeSwitch.isChecked = isDarkMode
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setUpClickListeners() {
        binding.themeSwitch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                // Block swipe gestures
                return@setOnTouchListener true
            }
            false // Allow other touch events like clicking
        }
        binding.themeSwitch.setOnClickListener {
            if (isDarkMode) {
                (requireActivity() as MainActivity).applyLightTheme()
            } else {
                (requireActivity() as MainActivity).applyDarkTheme()
            }
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