package com.example.dynamicwallpaper.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.Paging.WallpaperPagingAdapter
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.FragmentSearchBinding

class SearchFragment : BaseFragment<FragmentSearchBinding>() {
    private lateinit var pagingAdapter: WallpaperPagingAdapter
    private lateinit var viewModel: WallpaperViewModel
    private var query: String? = null
    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[WallpaperViewModel::class.java]
        query = arguments?.getString("query")
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {
        pagingAdapter = WallpaperPagingAdapter(::onItemClicked, ::onFavClick)
        with(binding) {
            rvSearchWallpapers.layoutManager = GridLayoutManager(requireContext(), 2)
            rvSearchWallpapers.setHasFixedSize(true)
            rvSearchWallpapers.adapter = pagingAdapter
            pagingAdapter.addLoadStateListener { loadState ->
                pb.visibility = if (loadState.source.append is LoadState.Loading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
        if (query != null) {
            binding.searchView.visibility = View.INVISIBLE
            binding.tvCategoryName.visibility = View.VISIBLE
            binding.tvCategoryName.text = query
            viewModel.searchWallpaper(query!!)
        }
    }

    private fun onFavClick(item: Photo) {
        viewModel.insertFavImage(FavouriteImageDataBase(item.src.portrait))
    }

    private fun onItemClicked(position: Int) {
        viewModel.selectedPosition = position
        findNavController().navigate(
            R.id.action_searchFragment_to_viewWallpaperFragment, bundleOf("source" to "search")
        )
    }

    override fun setUpClickListeners() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    binding.pb.visibility = View.VISIBLE
                    pagingAdapter.refresh()
                    viewModel.searchWallpaper(it)
                    binding.searchView.clearFocus()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        binding.imgBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun setUpObservers() {
        viewModel.searchedWallpapers.observe(viewLifecycleOwner) { pagingData ->
            pagingAdapter.submitData(lifecycle, pagingData)
        }
    }

    override fun onResume() {
        (requireActivity() as MainActivity).setBottomNavigationVisibility(false)
        super.onResume()
    }

    override fun onDestroyView() {
        (requireActivity() as MainActivity).setBottomNavigationVisibility(true)
        super.onDestroyView()
    }
}