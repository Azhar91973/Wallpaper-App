package com.example.dynamicwallpaper.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Common.CustomLoadStateAdapter
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.Paging.WallpaperPagingAdapter
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.FragmentHomeBinding
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: WallpaperViewModel by activityViewModels()
    private lateinit var pagingAdapter: WallpaperPagingAdapter
    private var lastSwipeTime = 0L
    private lateinit var drawerLayout: DrawerLayout

    companion object {
        const val NAVIGATION_SOURCE = "source"
        private const val SWIPE_DEBOUNCE_TIME = 300L
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?,
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpDrawerLayout()
        setUpViews()
        setUpClickListeners()
        setUpObservers()
        observeLoadState()
    }

    private fun setUpDrawerLayout() {
        drawerLayout = (requireActivity() as MainActivity).drawerLayout
        val navigationView: NavigationView = (requireActivity() as MainActivity).navigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.drawer_setting -> {
                    findNavController().navigate(R.id.action_HomeFragment_to_settingFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            true
        }
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

        detectSwipe()
    }

    private fun detectSwipe() {
        binding.rvWallpapers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (System.currentTimeMillis() - lastSwipeTime > SWIPE_DEBOUNCE_TIME) {
                    lastSwipeTime = System.currentTimeMillis()
                    if (dy > 0) setBottomNavigationVisibility(false)
                    else if (dy < 0) setBottomNavigationVisibility(true)
                }
            }
        })
    }

    private fun setBottomNavigationVisibility(isVisible: Boolean) {
        (requireActivity() as? MainActivity)?.setBottomNavigationVisibility(isVisible)
    }

    override fun setUpClickListeners() {
        binding.sv.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_searchFragment)
        }
        binding.icMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
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
    }

    private fun observeLoadState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingAdapter.loadStateFlow.collectLatest { loadState ->
                    handleLoadState(loadState)
                }
            }
        }
    }

    private fun handleLoadState(loadState: CombinedLoadStates) {
        val isListEmpty =
            loadState.source.refresh is LoadState.NotLoading && pagingAdapter.itemCount == 0
        val isLoading = loadState.source.refresh is LoadState.Loading
        val isError = loadState.source.refresh is LoadState.Error

        binding.rvWallpapers.visibility =
            if (isListEmpty || isError || isLoading) View.GONE else View.VISIBLE
        binding.loadStateLayout.visibility =
            if (isListEmpty || isError || isLoading) View.VISIBLE else View.GONE

        if (binding.loadStateLayout.childCount == 0) {
            val loadStateView = LayoutInflater.from(requireContext())
                .inflate(R.layout.load_state_footer, binding.loadStateLayout, false)
            binding.loadStateLayout.addView(loadStateView)
            bindLoadStateView(loadStateView, loadState.refresh)
        } else {
            val loadStateView = binding.loadStateLayout.getChildAt(0)
            bindLoadStateView(loadStateView, loadState.refresh)
        }
    }

    private fun bindLoadStateView(loadStateView: View, loadState: LoadState) {
        val retryButton: View = loadStateView.findViewById(R.id.retry_button)
        val progressBar: View = loadStateView.findViewById(R.id.progress_bar)
        val errorMsg: View = loadStateView.findViewById(R.id.error_msg)

        retryButton.setOnClickListener { pagingAdapter.retry() }

        progressBar.visibility = if (loadState is LoadState.Loading) View.VISIBLE else View.GONE
        errorMsg.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE
        retryButton.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE
    }

    private fun onFavClick(item: Photo) {
        viewModel.getImageByUrl(item.src.portrait) { isPresent ->
            if (!isPresent) {
                viewModel.insertFavImage(FavouriteImageDataBase(item.src.portrait))
                showToast(getString(R.string.image_added_to_favourite))
            } else {
                showToast(getString(R.string.image_already_in_fav))
            }
        }
    }

    private fun onItemClicked(position: Int) {
        Log.d("HomeFragment", "Selected Position: $position")
        viewModel.selectedPosition = position
        findNavController().navigate(
            R.id.action_HomeFragment_to_viewWallpaperFragment, bundleOf(NAVIGATION_SOURCE to "home")
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.recyclerViewState = binding.rvWallpapers.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.recyclerViewState?.let {
            binding.rvWallpapers.layoutManager?.onRestoreInstanceState(it)
        }
    }
}