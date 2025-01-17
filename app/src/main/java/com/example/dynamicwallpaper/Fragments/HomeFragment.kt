package com.example.dynamicwallpaper.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.Common.CustomLoadStateAdapter
import com.example.dynamicwallpaper.Paging.WallpaperPagingAdapter
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: WallpaperViewModel by activityViewModels()
    private lateinit var pagingAdapter: WallpaperPagingAdapter
    private var lastSwipeTime = 0L

    companion object {
        const val NAVIGATION_SOURCE = "source"
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detectSwipe()
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {
        pagingAdapter = WallpaperPagingAdapter(::onItemClicked, ::onFavClick)
        binding.rvWallpapers.apply {
            layoutManager = GridLayoutManager(requireContext(), 3).apply {
                initialPrefetchItemCount = 6
            }
            setHasFixedSize(true)
            adapter =
                pagingAdapter.withLoadStateFooter(CustomLoadStateAdapter { pagingAdapter.retry() })
        }
    }

    private fun onFavClick(item: Photo) {
        viewModel.getImageByUrl(item.src.portrait) { isPresent ->
            if (!isPresent) {
                viewModel.insertFavImage(FavouriteImageDataBase(item.src.portrait))
                showToast("Image Added To Favourites")
            } else {
                showToast("Image Already in Favourites")
            }
        }
    }

    private fun onItemClicked(position: Int) {
        Log.d("WallpaperPosition", "Selected Position: $position")
        viewModel.selectedPosition = position
        findNavController().navigate(
            R.id.action_HomeFragment_to_viewWallpaperFragment, bundleOf(NAVIGATION_SOURCE to "home")
        )
    }

    private fun detectSwipe() {
        binding.rvWallpapers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (System.currentTimeMillis() - lastSwipeTime > 300) {
                    lastSwipeTime = System.currentTimeMillis()
                    if (dy > 0) onSwipeUp() else if (dy < 0) onSwipeDown()
                }
            }
        })
    }

    private fun onSwipeUp() {
        if ((requireActivity() as MainActivity).getBottomNavStatus()) {
            (requireActivity() as MainActivity).setBottomNavigationVisibility(false)
        }
    }

    private fun onSwipeDown() {
        if (!(requireActivity() as MainActivity).getBottomNavStatus()) {
            (requireActivity() as MainActivity).setBottomNavigationVisibility(true)
        }
    }

    override fun setUpClickListeners() {
        binding.sv.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_searchFragment)
        }
    }

    override fun setUpObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wallpapers.collectLatest { wallpapers ->
                    pagingAdapter.submitData(lifecycle, wallpapers)
                }
            }
        }

        lifecycleScope.launch {
            pagingAdapter.loadStateFlow.collectLatest { loadState ->
                val errorState = loadState.source.refresh as? androidx.paging.LoadState.Error
                errorState?.let {
                    showToast("Error loading data: ${it.error.localizedMessage}")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.recyclerViewState = binding.rvWallpapers.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.recyclerViewState?.let {
            if (pagingAdapter.itemCount > 0) {
                binding.rvWallpapers.layoutManager?.onRestoreInstanceState(it)
            }
        }
    }
}