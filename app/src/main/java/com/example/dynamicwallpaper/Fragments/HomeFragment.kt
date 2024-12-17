package com.example.dynamicwallpaper.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dynamicwallpaper.Common.BaseAdapter
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.Models.CategoryItems
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.Paging.WallpaperPagingAdapter
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.CategoryItemBinding
import com.example.dynamicwallpaper.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private lateinit var viewModel: WallpaperViewModel
    private lateinit var pagingAdapter: WallpaperPagingAdapter
    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[WallpaperViewModel::class.java]
        detectSwipe()
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {
        setUpCategoryItems()
        pagingAdapter = WallpaperPagingAdapter(::onItemClicked, ::onFavClick)
        with(binding) {
            rvWallpapers.layoutManager = GridLayoutManager(requireContext(), 3)
            rvWallpapers.setHasFixedSize(true)
            rvWallpapers.adapter = pagingAdapter
            pagingAdapter.addLoadStateListener { loadState ->
                pb.visibility = if (loadState.source.append is LoadState.Loading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun onFavClick(item: Photo) {
        viewModel.insertFavImage(FavouriteImageDataBase(item.src.portrait))
    }

    private fun onItemClicked(position: Int) {
        viewModel.selectedPosition = position
        findNavController().navigate(
            R.id.action_HomeFragment_to_viewWallpaperFragment, bundleOf("source" to "home")
        )

    }

    private fun setUpCategoryItems() {
        val items = listOf(
            CategoryItems(
                "https://images.pexels.com/photos/1283208/pexels-photo-1283208.jpeg", "Abstract"
            ), CategoryItems(
                "https://images.pexels.com/photos/3165335/pexels-photo-3165335.jpeg", "Gaming"
            ), CategoryItems(
                "https://images.pexels.com/photos/216798/pexels-photo-216798.jpeg", "Nature"
            ), CategoryItems(
                "https://images.pexels.com/photos/1540406/pexels-photo-1540406.jpeg", "Music"
            )
        )

        val cAdapter = BaseAdapter<CategoryItems>()
        cAdapter.listOfItems = items.toMutableList()
        cAdapter.expressionViewHolderBinding = { item, viewBinding ->
            val view = viewBinding as CategoryItemBinding
            Glide.with(requireContext()).load(item.categoryImgUrl).into(view.imgCategory)
            view.tvCategory.text = item.categoryName
            view.imgCategory.setOnClickListener {
                findNavController().navigate(
                    R.id.action_HomeFragment_to_searchFragment,
                    bundleOf("query" to item.categoryName)
                )
            }
        }
        cAdapter.expressionOnCreateViewHolder = {
            CategoryItemBinding.inflate(layoutInflater, it, false)
        }
        binding.rvCategory.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategory.adapter = cAdapter

    }

    private fun detectSwipe() {
        binding.rvWallpapers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    onSwipeUp()
                } else if (dy < 0) {
                    onSwipeDown()
                }
            }
        })
    }

    private fun onSwipeUp() {
        if ((requireActivity() as MainActivity).getBottomNavStatus()) (requireActivity() as MainActivity).setBottomNavigationVisibility(
            false
        )
    }

    private fun onSwipeDown() {
        if (!(requireActivity() as MainActivity).getBottomNavStatus()) (requireActivity() as MainActivity).setBottomNavigationVisibility(
            true
        )
    }

    override fun setUpClickListeners() {

        binding.sv.setOnQueryTextFocusChangeListener { _, _ ->
            val currentDestination = findNavController().currentDestination?.id
            if (currentDestination != R.id.searchFragment) {
                findNavController().navigate(R.id.action_HomeFragment_to_searchFragment)
            }
        }
        binding.tvViewAll.setOnClickListener {
            (requireActivity() as MainActivity).navigateToFragment(R.id.categoryFragment, 1)
        }
    }

    override fun setUpObservers() {
        viewModel.wallpapers.observe(viewLifecycleOwner) {
            pagingAdapter.submitData(lifecycle, it)
        }
    }


    override fun onPause() {
        super.onPause()
        // Saving the state/position of recyclerView
        viewModel.recyclerViewState = binding.rvWallpapers.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        // setting the state/position of recyclerView
        viewModel.recyclerViewState?.let {
            binding.rvWallpapers.layoutManager?.onRestoreInstanceState(it)
        }
    }
}