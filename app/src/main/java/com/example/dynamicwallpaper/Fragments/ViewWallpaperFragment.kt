package com.example.dynamicwallpaper.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.example.dynamicwallpaper.Common.BaseAdapter
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Common.SharedPrefs
import com.example.dynamicwallpaper.Common.SharedPrefs.Companion.THEME_DARK
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.MyBottomSheetFragment
import com.example.dynamicwallpaper.Paging.ViewWallpaperPagingAdapter
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.WallpaperService.WallpaperHelper
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.FragmentViewWallpapperBinding
import com.example.dynamicwallpaper.databinding.SetWallpaperItemBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ViewWallpaperFragment : BaseFragment<FragmentViewWallpapperBinding>() {

    private lateinit var pagingAdapter: ViewWallpaperPagingAdapter
    private lateinit var favAdapter: BaseAdapter<FavouriteImageDataBase, SetWallpaperItemBinding>
    private val wallpaperViewModel: WallpaperViewModel by activityViewModels()
    private lateinit var source: String
    private var isDarkMode: Boolean = false

    @Inject
    lateinit var sharedPrefs: SharedPrefs

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?,
    ): FragmentViewWallpapperBinding {
        return FragmentViewWallpapperBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pagingAdapter = ViewWallpaperPagingAdapter(
            ::setWallpaper, ::favImage, ::downloadImage
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFullScreenMode()
        (requireActivity() as MainActivity).setBottomNavigationVisibility(false)
        source = arguments?.getString(ARG_SOURCE) ?: "home"
        setUpViews()
        setUpClickListeners()
        setUpObservers()
        observerLoadState()
    }

    private fun setupFullScreenMode() {
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        val window = requireActivity().window
//
//        // Let your content lay out behind system bars
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//        val controller = WindowInsetsControllerCompat(window, window.decorView)
//
//        // Hide both status and navigation bars
//        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
//
//        // Optional but recommended: make it immersive so they only appear on swipe
//        controller.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun setStatusBarIcons(isLightTheme: Boolean) {
        val window = requireActivity().window
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = isLightTheme
        }
    }

    override fun setUpViews() {
        binding.vpViewWallpaper.adapter = pagingAdapter
    }

    override fun setUpClickListeners() {
        binding.imgBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun setUpObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                when (source) {
                    "search" -> collectSearchedWallpapers()
                    "home" -> collectWallpapers()
                    "Favourite" -> observeFavWallpapers()
                }
            }
        }
    }

    private fun favImage(item: Photo) {
        lifecycleScope.launch {
            wallpaperViewModel.getImageByUrl(item.src.portrait) { isPresent ->
                if (!isPresent) {
                    wallpaperViewModel.insertFavImage(FavouriteImageDataBase(item.src.portrait))
                    showToast(getString(R.string.image_added_to_favourite))
                } else {
                    showToast(getString(R.string.image_already_in_fav))
                }
            }
        }
    }

    private fun downloadImage(item: Photo) {
        lifecycleScope.launch {
            WallpaperHelper(requireContext()).downloadWallpaper(
                requireContext(), item.src.portrait, item.id.toString()
            )
        }
    }

    private fun setWallpaper(imageUrl: String) {
        wallpaperViewModel.resetSelectedWallpapers()
        WorkManager.getInstance(requireContext()).cancelAllWork()

        MyBottomSheetFragment.newInstance(imageUrl).show(
            requireActivity().supportFragmentManager, "BottomSheet"
        )
    }

    private fun observerLoadState() {
        pagingAdapter.addLoadStateListener { loadStates ->
            val isNotLoading = loadStates.refresh !is androidx.paging.LoadState.Loading
            if (isNotLoading) {
                val position = wallpaperViewModel.selectedPosition ?: 0
                binding.vpViewWallpaper.setCurrentItem(position, false)
            }
        }
    }

    private fun collectWallpapers() {
        lifecycleScope.launch {
            wallpaperViewModel.wallpapers.collectLatest { pagingData ->
                pagingAdapter.submitData(lifecycle, pagingData)
            }
        }
    }

    private fun collectSearchedWallpapers() {
        lifecycleScope.launch {
            wallpaperViewModel.searchedWallpapers.collectLatest { pagingData ->
                pagingData.let { pagingAdapter.submitData(lifecycle, it) }
            }
        }
    }


    private fun observeFavWallpapers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                wallpaperViewModel.favWallpapers.collectLatest { favList ->
                    showFavImages(favList)
                }
            }
        }
    }

    private fun showFavImages(favList: List<FavouriteImageDataBase>) {
        favAdapter = object : BaseAdapter<FavouriteImageDataBase, SetWallpaperItemBinding>() {
            override fun createBinding(parent: ViewGroup): SetWallpaperItemBinding {
                return SetWallpaperItemBinding.inflate(layoutInflater, parent, false)
            }

            override fun getItemId(item: FavouriteImageDataBase): String {
                // Convert the item's id to String (adjust as needed)
                return item.id.toString()
            }

            override fun bindView(
                binding: SetWallpaperItemBinding,
                item: FavouriteImageDataBase,
                isSelected: Boolean,
            ) {
                // Set the view properties (isSelected not used in this case)
                binding.constraintLayout.visibility = View.GONE
                Glide.with(requireContext()).load(item.imageUrl).into(binding.viewWallpaper)
            }

            override fun areItemsTheSame(
                oldItem: FavouriteImageDataBase,
                newItem: FavouriteImageDataBase,
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: FavouriteImageDataBase,
                newItem: FavouriteImageDataBase,
            ): Boolean {
                return oldItem == newItem
            }
        }

        favAdapter.submitList(favList)
        binding.vpViewWallpaper.adapter = favAdapter
        binding.vpViewWallpaper.setCurrentItem(wallpaperViewModel.selectedPosition ?: 0, false)
    }


    override fun onPause() {
        super.onPause()
        Log.d("ViewWallpaperFragment", "called onPause")
        wallpaperViewModel.selectedPosition = binding.vpViewWallpaper.currentItem
    }

    override fun onResume() {
        super.onResume()
        Log.d("ViewWallpaperFragment", "called onResume")
        isDarkMode = sharedPrefs.getThemePreference() == THEME_DARK
        setStatusBarIcons(isDarkMode)
        (requireActivity() as MainActivity).setBottomNavigationVisibility(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ViewWallpaperFragment", "called onDestroyView")
        resetSystemUiVisibility()
        if (source != "search") {
            (requireActivity() as MainActivity).setBottomNavigationVisibility(true)
        }
        setStatusBarIcons(!isDarkMode)
    }

    private fun resetSystemUiVisibility() {
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    companion object {
        const val ARG_SOURCE = "source"
    }
}