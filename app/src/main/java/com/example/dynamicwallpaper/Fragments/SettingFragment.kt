package com.example.dynamicwallpaper.Fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.databinding.FragmentSettingBinding


class SettingFragment : BaseFragment<FragmentSettingBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
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
        Log.d(ContentValues.TAG, "setUpViews: SettingFragment")
    }

    override fun setUpClickListeners() {
    }

    override fun setUpObservers() {
    }
}