package com.example.dynamicwallpaper.Fragments

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.Paging.ViewWallpaperPagingAdapter
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.FragmentViewWallpapperBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ViewWallpaperFragment : BaseFragment<FragmentViewWallpapperBinding>() {

    private lateinit var pagingAdapter: ViewWallpaperPagingAdapter
    private lateinit var wallpaperViewModel: WallpaperViewModel
    private lateinit var wallpaperManager: WallpaperManager
    lateinit var source: String
    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentViewWallpapperBinding {
        return FragmentViewWallpapperBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Enable edge-to-edge for this fragment
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        (requireActivity() as MainActivity).setBottomNavigationVisibility(false)
        source = arguments?.getString("source") ?: "home"
        wallpaperViewModel = ViewModelProvider(requireActivity())[WallpaperViewModel::class.java]
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {
        wallpaperManager = WallpaperManager.getInstance(context)
        // Initialize adapter
        pagingAdapter =
            ViewWallpaperPagingAdapter(::setWallpaper, ::favImage, ::shareImage, ::backBtnClicked)
        with(binding) {
            vpViewWallpaper.adapter = pagingAdapter
            vpViewWallpaper.adapter = pagingAdapter
        }
    }

    private fun favImage(imageUrl: String) {
        wallpaperViewModel.insertFavImage(FavouriteImageDataBase(imageUrl))
        showSnackBar("Image Added to Favourite")
    }

    private fun shareImage(imageUrl: String) {
        // TODO Implement Share Function
    }

    private fun setWallpaper(imageUrl: String) {
        lifecycleScope.launch {
            Glide.with(requireContext()).asBitmap().load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(object : CustomTarget<Bitmap>() {
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

    private fun backBtnClicked() {
        findNavController().navigateUp()
    }

    override fun setUpClickListeners() {}
    override fun setUpObservers() {
        pagingAdapter.refresh() // Clear any previous data

        if (source == "search") {
            wallpaperViewModel.searchedWallpapers.observe(viewLifecycleOwner) { pagingData ->
                pagingAdapter.submitData(lifecycle, pagingData)
            }
        } else {
            wallpaperViewModel.wallpapers.observe(viewLifecycleOwner) { pagingData ->
                pagingAdapter.submitData(lifecycle, pagingData)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Saving the state/position of recyclerView
        wallpaperViewModel.selectedPosition = binding.vpViewWallpaper.currentItem
    }

    override fun onResume() {
        super.onResume()
        // setting the state/position of recyclerView
        wallpaperViewModel.selectedPosition.let {
            binding.vpViewWallpaper.setCurrentItem(wallpaperViewModel.selectedPosition!!, false)
        }
        (requireActivity() as MainActivity).setBottomNavigationVisibility(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        if (source != "search") (requireActivity() as MainActivity).setBottomNavigationVisibility(
            true
        )
    }
}