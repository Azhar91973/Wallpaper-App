package com.example.dynamicwallpaper.Fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.dynamicwallpaper.Common.BaseAdapter
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.ViewWallpaperActivity
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.FragmentSearchBinding
import com.example.dynamicwallpaper.databinding.WallpaperItemBinding
import com.google.gson.Gson

class SearchFragment : BaseFragment<FragmentSearchBinding>() {
    private lateinit var rvLayoutManager: GridLayoutManager
    private lateinit var adapter: BaseAdapter<Photo>
    private var page = 1
    private lateinit var viewModel: WallpaperViewModel
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private var cachedWallpaper = mutableListOf<Photo>()
    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[WallpaperViewModel::class.java]
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {
        adapter = BaseAdapter()
        rvLayoutManager = GridLayoutManager(requireContext(), 3)

    }

    private fun setUpWallpapers(wallpapers: List<Photo>) {

        adapter.listOfItems = wallpapers.toMutableList()
        adapter.expressionOnCreateViewHolder = {
            WallpaperItemBinding.inflate(layoutInflater, it, false)
        }
        adapter.expressionViewHolderBinding = { item, viewBinding ->
            val view = viewBinding as WallpaperItemBinding
            Glide.with(requireContext()).load(item.src.portrait).into(view.imgWallpaper)
            view.imgWallpaper.setOnClickListener {
                startActivityWithDestination(
                    "ViewWallpaperFragment", cachedWallpaper, cachedWallpaper.indexOf(item), page
                )
            }
            view.icFav.setOnClickListener {
                viewModel.insertFavImage(FavouriteImageDataBase(item.src.portrait))
                showSnackBar("Added to favourites")
            }
        }
        binding.rvSearchWallpapers.layoutManager = rvLayoutManager
        binding.rvSearchWallpapers.adapter = adapter
        Log.d(ContentValues.TAG, "setUpWallpapers: $wallpapers")
    }

    private fun startActivityWithDestination(
        destination: String,
        cachedWallpaper: List<Photo>? = null,
        position: Int? = 0,
        page: Int? = 0
    ) {
        val intent = Intent(requireContext(), ViewWallpaperActivity::class.java).apply {
            putExtra("destination", destination)
            val gson = Gson()
            putExtra("cachedWallpaper", gson.toJson(cachedWallpaper))
            putExtra("position", position)
            putExtra("page", page)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startForResult.launch(intent)
    }

    override fun setUpClickListeners() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.getWallpapers(page++, it, "search") }
                binding.searchView.setQuery("", false)
                binding.searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                cachedWallpaper.clear()
                return true
            }
        })

    }

    override fun setUpObservers() {
        viewModel.searchWallpapers.observe(viewLifecycleOwner) {
            cachedWallpaper.addAll(it.photos)
            setUpWallpapers(cachedWallpaper)
        }
    }

}