package com.example.dynamicwallpaper.Fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.dynamicwallpaper.Common.BaseAdapter
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.FragmentFavouriteBinding
import com.example.dynamicwallpaper.databinding.WallpaperItemBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavouriteFragment : BaseFragment<FragmentFavouriteBinding>() {
    private lateinit var viewModel: WallpaperViewModel
    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentFavouriteBinding {
        return FragmentFavouriteBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[WallpaperViewModel::class.java]
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {
        // Getting Favourite Images from room database
        viewModel.getAllFavImages()
    }

    private fun setUpWallpapers(wallpapers: List<FavouriteImageDataBase>) {
        val adapter = BaseAdapter<FavouriteImageDataBase>()
        adapter.listOfItems = wallpapers.toMutableList()
        adapter.expressionOnCreateViewHolder = {
            WallpaperItemBinding.inflate(layoutInflater, it, false)
        }
        adapter.expressionViewHolderBinding = { item, viewBinding ->
            val view = viewBinding as WallpaperItemBinding
            Glide.with(requireContext()).load(item.imageUrl).into(view.imgWallpaper)
            view.icFav.visibility = View.GONE
        }
        binding.rvFavWallpapers.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvFavWallpapers.adapter = adapter
    }

    override fun setUpClickListeners() {
    }

    override fun setUpObservers() {
        viewModel.favWallpapers.observe(viewLifecycleOwner) {
            setUpWallpapers(it)
        }
    }
}