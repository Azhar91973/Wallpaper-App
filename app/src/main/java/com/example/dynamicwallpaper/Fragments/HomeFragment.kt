package com.example.dynamicwallpaper.Fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codebyashish.autoimageslider.Enums.ImageActionTypes
import com.codebyashish.autoimageslider.Interfaces.ItemsListener
import com.example.dynamicwallpaper.Common.BaseAdapter
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.Models.CategoryItems
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.ViewWallpaperActivity
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.CategoryItemBinding
import com.example.dynamicwallpaper.databinding.FragmentHomeBinding
import com.example.dynamicwallpaper.databinding.WallpaperItemBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), ItemsListener {
    private var listener: ItemsListener? = null
    private var page = 1
    private var isLoading = false
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private lateinit var viewModel: WallpaperViewModel
    private lateinit var rvLayoutManager: GridLayoutManager
    private lateinit var adapter: BaseAdapter<Photo>
    private var cachedWallpaper = mutableListOf<Photo>()
    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[WallpaperViewModel::class.java]
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        setUpViews()
        setUpClickListeners()
        setUpObservers()
        observeRecyclerViewScroll()
    }

    private fun observeRecyclerViewScroll() {
        binding.rvWallpapers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) { // Only check if scrolling down
                    val visibleItemCount = rvLayoutManager.childCount
                    val totalItemCount = rvLayoutManager.itemCount
                    val pastVisibleItems = rvLayoutManager.findFirstVisibleItemPosition()

                    // Get the first visible item position from the staggered grid
                    val lastVisibleItemPosition = pastVisibleItems ?: 0

                    // Check if we've reached the last item
                    if (!isLoading && (visibleItemCount + lastVisibleItemPosition) >= totalItemCount) {
                        // Set isLoading to true to prevent multiple triggers
                        isLoading = true
                        // Trigger the loading of more data
                        showSnackBar("Reached the end of the list")
                        page += 1
                        viewModel.getWallpapers(
                            page, null, "curated"
                        )  // Fetch new data (pagination)
                    }
                }
            }
        })
    }

    override fun setUpViews() {
//        setUpImageSlider()
        setUpCategoryItems()
        adapter = BaseAdapter()
        rvLayoutManager = GridLayoutManager(requireContext(), 3)
        viewModel.getWallpapers(page++, null, "featured")
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
        binding.rvWallpapers.layoutManager = rvLayoutManager
        binding.rvWallpapers.adapter = adapter
        Log.d(TAG, "setUpWallpapers: $wallpapers")
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

    private fun setUpCategoryItems() {
        val items = listOf(
            CategoryItems(
                "https://images.pexels.com/photos/27916602/pexels-photo-27916602/free-photo-of-a-man-on-a-motorcycle-is-sitting-in-the-middle-of-a-street.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Abstract"
            ), CategoryItems(
                "https://images.pexels.com/photos/28762715/pexels-photo-28762715/free-photo-of-stunning-colorful-sandstone-formations-in-antelope-canyon.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Gaming"
            ), CategoryItems(
                "https://images.pexels.com/photos/28818953/pexels-photo-28818953/free-photo-of-charming-cafe-exterior-with-vintage-signage.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Nature"
            ), CategoryItems(
                "https://images.pexels.com/photos/28762715/pexels-photo-28762715/free-photo-of-stunning-colorful-sandstone-formations-in-antelope-canyon.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Gaming"
            ), CategoryItems(
                "https://images.pexels.com/photos/27916602/pexels-photo-27916602/free-photo-of-a-man-on-a-motorcycle-is-sitting-in-the-middle-of-a-street.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Abstract"
            )
        )

        val cAdapter = BaseAdapter<CategoryItems>()
        cAdapter.listOfItems = items.toMutableList()
        cAdapter.expressionViewHolderBinding = { item, viewBinding ->
            val view = viewBinding as CategoryItemBinding
            Glide.with(requireContext()).load(item.categoryImgUrl).into(view.imgCategory)
            view.tvCategory.text = item.categoryName
        }
        cAdapter.expressionOnCreateViewHolder = {
            CategoryItemBinding.inflate(layoutInflater, it, false)
        }
        binding.rvCategory.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategory.adapter = cAdapter

    }

//    private fun setUpImageSlider() {
//        listener = this
//        val autoImageList: ArrayList<ImageSlidesModel> = ArrayList()
//        val autoImageSlider = binding.autoImageSlider
//        // TODO : Fetch images from API
//        autoImageList.add(ImageSlidesModel("https://images.pexels.com/photos/27916602/pexels-photo-27916602/free-photo-of-a-man-on-a-motorcycle-is-sitting-in-the-middle-of-a-street.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"))
//        autoImageList.add(ImageSlidesModel("https://images.pexels.com/photos/28762715/pexels-photo-28762715/free-photo-of-stunning-colorful-sandstone-formations-in-antelope-canyon.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"))
//        autoImageList.add(ImageSlidesModel("https://images.pexels.com/photos/28818953/pexels-photo-28818953/free-photo-of-charming-cafe-exterior-with-vintage-signage.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"))
//
//        autoImageSlider.setImageList(autoImageList, ImageScaleType.FIT)
//
//        // set any default animation or custom animation (setSlideAnimation(ImageAnimationTypes.ZOOM_IN))
//        autoImageSlider.setDefaultAnimation()
//
//        // handle click event on item click
//        autoImageSlider.onItemClickListener(listener)
//    }

    override fun setUpClickListeners() {
//        binding.searchView.setOnQueryTextFocusChangeListener { _, _ ->
//            binding.backBtn.visibility = View.VISIBLE
//            Log.d("NavController", "Current Destination: ${findNavController().currentDestination?.parent }  ${findNavController().previousBackStackEntry?.destination} ")
//
//                    val currentDestination = findNavController().currentDestination?.id
//                    if (currentDestination == R.id.HomeFragment) {
//                        findNavController().navigate(R.id.action_homeFragment_to_searchWallpaperFragment)
//                    } else {
//                        val navController = findNavController()
//                        Log.d("NavigationDebug", "Current Destination: ${navController.currentDestination?.label}")
//
//                        Log.d("NavigationError", "Cannot navigate from the current destination")
//                    }
//
//        }
    }

    override fun setUpObservers() {
        viewModel.wallpapers.observe(viewLifecycleOwner) {
            Log.d(TAG, "setUpObservers: ${it.photos}")
            cachedWallpaper.addAll(it.photos)
            if (page == 2) setUpWallpapers(cachedWallpaper)
            else adapter.updateItems(cachedWallpaper)
            isLoading = false
        }
        viewModel.error.observe(viewLifecycleOwner) {
            showSnackBar(it)
        }
    }

    override fun onItemChanged(position: Int) {

    }

    override fun onItemClicked(position: Int) {

    }

    override fun onTouched(actionTypes: ImageActionTypes?, position: Int) {
    }
}