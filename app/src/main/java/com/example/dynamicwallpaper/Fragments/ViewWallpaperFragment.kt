package com.example.dynamicwallpaper.Fragments

import android.app.WallpaperManager
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.dynamicwallpaper.Common.BaseAdapter
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.FragmentViewWallpapperBinding
import com.example.dynamicwallpaper.databinding.SetWallpaperItemBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewWallpaperFragment : BaseFragment<FragmentViewWallpapperBinding>() {

    private var cachedWallpaper = mutableListOf<Photo>()
    private lateinit var adapter: BaseAdapter<Photo>
    private lateinit var wallpaperViewModel: WallpaperViewModel
    private var position: Int? = null
    private var page: Int? = 1
    private lateinit var wallpaperManager: WallpaperManager
    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentViewWallpapperBinding {
        return FragmentViewWallpapperBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wallpaperGson = arguments?.getString("cachedWallpaper")
        position = arguments?.getInt("position")
        page = arguments?.getInt("page")
        if (wallpaperGson != null) {
            cachedWallpaper =
                Gson().fromJson(wallpaperGson, Array<Photo>::class.java).toMutableList()
        }
        wallpaperViewModel = ViewModelProvider(requireActivity())[WallpaperViewModel::class.java]
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {
        wallpaperManager = WallpaperManager.getInstance(context)
        // Initialize adapter
        adapter = BaseAdapter()
        adapter.listOfItems = cachedWallpaper
        adapter.expressionOnCreateViewHolder = {
            SetWallpaperItemBinding.inflate(layoutInflater, it, false)
        }
        adapter.expressionViewHolderBinding = { item, viewBinding ->
            val view = viewBinding as SetWallpaperItemBinding

            // Load the wallpaper using Glide
            Glide.with(requireContext()).load(item.src.portrait).into(view.viewWallpaper)

            // Set wallpaper on button click
            view.imgSetWallpaper.setOnClickListener {
                setWallpaper(item.src.portrait)
            }
            view.imgAddFavourite.setOnClickListener {
                wallpaperViewModel.insertFavImage(FavouriteImageDataBase(item.src.portrait))
                showSnackBar("Added to Favourite")
            }
        }

        // Set adapter to ViewPager2
        binding.vpViewWallpaper.adapter = adapter
        position?.let { binding.vpViewWallpaper.setCurrentItem(it, false) }

        // Page change listener for pagination
        binding.vpViewWallpaper.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Load next page when reaching the end of the current page
                if (position == adapter.itemCount - 1) {
                    showSnackBar("Loading more wallpapers...")
                    page = page!! + 1
                    wallpaperViewModel.getWallpapers(page!!, null, "curated")
                }
            }
        })
    }

    private fun setWallpaper(imageUrl: String) {
        // Use Glide to download the image and set it as wallpaper
        lifecycleScope.launch {
            Glide.with(requireContext()).asBitmap().load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)  // Ensure caching
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        wallpaperManager.setBitmap(resource)  // Set the wallpaper
                        showSnackBar("Wallpaper Set Successfully!")
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle image load being cleared
                    }
                })
        }
    }

    override fun setUpClickListeners() {}
    override fun setUpObservers() {
        wallpaperViewModel.wallpapers.observe(viewLifecycleOwner) { wallpapers ->
            if (wallpapers != null) {
                val updatedWallpapers = ArrayList(cachedWallpaper) // Create a copy
                updatedWallpapers.addAll(wallpapers.photos) // Add new items
                adapter.updateItems(updatedWallpapers) // Update adapter items
                cachedWallpaper = updatedWallpapers // Update the reference
                Log.d(TAG, "Updated adapter.listOfItems: ${adapter.listOfItems.size}")
                Log.d(TAG, "Updated cachedWallpaper: ${cachedWallpaper.size}")
            }
            Log.d(TAG, "Page : $page")
        }
        wallpaperViewModel.error.observe(viewLifecycleOwner) {
            showSnackBar(it)
        }
    }

}