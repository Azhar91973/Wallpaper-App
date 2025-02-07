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

    // Shared ViewModel instance
    private val viewModel: WallpaperViewModel by activityViewModels()

    // Adapter for paging and displaying wallpapers
    private lateinit var pagingAdapter: WallpaperPagingAdapter

    // Navigation drawer layout
    private lateinit var drawerLayout: DrawerLayout

    companion object {
        private const val NAVIGATION_SOURCE = "source"
        private const val SWIPE_DEBOUNCE_TIME = 300L
        private const val GRID_SPAN_COUNT = 3
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?,
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup UI components
        setupDrawerLayout()
        setUpViews()
        setUpClickListeners()
        setUpObservers()
        observeLoadState()
    }

    /**
     * Configures the navigation drawer layout and handles menu item clicks.
     */
    private fun setupDrawerLayout() {
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

    /**
     * Initializes RecyclerView with a GridLayoutManager and attaches the paging adapter.
     */
    override fun setUpViews() {
        pagingAdapter = WallpaperPagingAdapter(::onItemClicked, ::onFavClick)

        binding.rvWallpapers.apply {
            layoutManager = GridLayoutManager(requireContext(), GRID_SPAN_COUNT)
            setHasFixedSize(true)
            adapter =
                pagingAdapter.withLoadStateFooter(CustomLoadStateAdapter { pagingAdapter.retry() })
        }

        handleRecyclerViewScroll()
    }

    /**
     * Handles RecyclerView scroll events to hide/show the bottom navigation bar.
     */
    private fun handleRecyclerViewScroll() {
        binding.rvWallpapers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var lastSwipeTime = 0L

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (System.currentTimeMillis() - lastSwipeTime > SWIPE_DEBOUNCE_TIME) {
                    lastSwipeTime = System.currentTimeMillis()
                    setBottomNavigationVisibility(dy < 0)
                }
            }
        })
    }

    /**
     * Shows or hides the bottom navigation bar based on scroll direction.
     */
    private fun setBottomNavigationVisibility(isVisible: Boolean) {
        (requireActivity() as? MainActivity)?.setBottomNavigationVisibility(isVisible)
    }

    /**
     * Sets up click listeners for UI elements such as the search bar and menu button.
     */
    override fun setUpClickListeners() {
        binding.sv.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_searchFragment)
        }

        binding.icMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    /**
     * Observes the wallpaper data from ViewModel and submits it to the adapter.
     */
    override fun setUpObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wallpapers.collectLatest { wallpapers ->
                    pagingAdapter.submitData(lifecycle, wallpapers)
                }
            }
        }
    }

    /**
     * Observes the adapter's load state and updates UI accordingly.
     */
    private fun observeLoadState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingAdapter.loadStateFlow.collectLatest { loadState ->
                    handleLoadState(loadState)
                }
            }
        }
    }

    /**
     * Handles different states of data loading (loading, error, success).
     */
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
            bindLoadStateView(binding.loadStateLayout.getChildAt(0), loadState.refresh)
        }
    }

    /**
     * Configures the load state view (retry button, progress bar, error message).
     */
    private fun bindLoadStateView(loadStateView: View, loadState: LoadState) {
        val retryButton: View = loadStateView.findViewById(R.id.retry_button)
        val progressBar: View = loadStateView.findViewById(R.id.progress_bar)
        val errorMsg: View = loadStateView.findViewById(R.id.error_msg)

        retryButton.setOnClickListener { pagingAdapter.retry() }

        progressBar.visibility = if (loadState is LoadState.Loading) View.VISIBLE else View.GONE
        errorMsg.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE
        retryButton.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE
    }

    /**
     * Handles the favorite button click to add/remove images from favorites.
     */
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

    /**
     * Handles wallpaper item click and navigates to the ViewWallpaperFragment.
     */
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