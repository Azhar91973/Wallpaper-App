package com.example.dynamicwallpaper.Fragments

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.example.dynamicwallpaper.Common.BaseAdapter
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Common.SharedPrefs
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.WallpaperService.WallpaperHelper.Companion.BOTH_SCREENS
import com.example.dynamicwallpaper.WallpaperService.WallpaperHelper.Companion.HOME_SCREEN
import com.example.dynamicwallpaper.WallpaperService.WallpaperHelper.Companion.LOCK_SCREEN
import com.example.dynamicwallpaper.WallpaperService.WallpaperWorker
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.FragmentFavouriteBinding
import com.example.dynamicwallpaper.databinding.WallpaperItemBinding
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class FavouriteFragment : BaseFragment<FragmentFavouriteBinding>() {
    private val viewModel: WallpaperViewModel by activityViewModels()
    private lateinit var adapter: BaseAdapter<FavouriteImageDataBase>
    private lateinit var drawerLayout: DrawerLayout
    private var isSelected: Boolean = false
    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?,
    ): FragmentFavouriteBinding {
        return FragmentFavouriteBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpDrawerLayout()
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {}

    private fun setUpWallpapers(wallpapers: List<FavouriteImageDataBase>) {
        adapter = BaseAdapter()
        adapter.listOfItems = wallpapers.toMutableList()
        adapter.expressionOnCreateViewHolder = {
            WallpaperItemBinding.inflate(layoutInflater, it, false)
        }
        adapter.expressionViewHolderBinding = { item, viewBinding ->
            val view = viewBinding as WallpaperItemBinding
            val idx = adapter.listOfItems.indexOf(item)

            // Bind data
            Glide.with(requireContext()).load(item.imageUrl).into(view.imgWallpaper)
            view.icFav.visibility = View.GONE

            // Handle selection state
            if (adapter.selectedItemList.contains(idx)) {
                view.selectedFav.visibility = View.VISIBLE
            } else {
                view.selectedFav.visibility = View.GONE
            }
            // Handle click and long click listeners
            view.imgWallpaper.setOnClickListener {
                if (adapter.selectedItemList.contains(idx)) {
                    toggleSelection(idx)
                } else if (isSelected) {
                    toggleSelection(idx)
                } else {
                    viewModel.selectedPosition = idx
                    Log.d("FavPosition", "setUpWallpapers: Position = $idx")
                    findNavController().navigate(
                        R.id.action_favouriteFragment_to_viewWallpaperFragment,
                        bundleOf("source" to "Favourite")
                    )
                }
            }
            view.imgWallpaper.setOnLongClickListener {
                if (!adapter.selectedItemList.contains(idx)) {
                    toggleSelection(idx)
                    isSelected = true
                    binding.btnDelete.visibility = View.VISIBLE
                }
                true
            }
        }

        binding.rvFavWallpapers.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvFavWallpapers.adapter = adapter
    }

    private fun setUpDrawerLayout() {
        drawerLayout = (requireActivity() as MainActivity).drawerLayout
        val navigationView: NavigationView = (requireActivity() as MainActivity).navigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.drawer_setting -> {
                    findNavController().navigate(R.id.action_favouriteFragment_to_settingFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            true
        }
    }

    override fun setUpClickListeners() {
        binding.btnDelete.setOnClickListener {
            val indicesToRemove = adapter.selectedItemList.sortedDescending()
            indicesToRemove.forEach { index ->
                viewModel.deleteFavImage(adapter.listOfItems[index])
                adapter.listOfItems.removeAt(index)
                adapter.notifyItemRemoved(index)
            }
            isSelected = false
            adapter.selectedItemList.clear()
            updateDeleteButtonVisibility()
            if (adapter.listOfItems.isEmpty()) setUpEmptyFavLayout()
        }
        binding.option.setOnClickListener {
            if (adapter.selectedItemList.size > 1) openDialog()
            else showToast(getString(R.string.play_wallpaper_info))
        }
        binding.icMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.btnExplore.setOnClickListener {
            (requireActivity() as MainActivity).onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun openDialog() {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.custom_dialog)
        }
        // Initialize views
        val tvQuantity = dialog.findViewById<TextView>(R.id.tv_quantity)
        val btnIncrement = dialog.findViewById<ImageView>(R.id.btn_increment)
        val btnDecrement = dialog.findViewById<ImageView>(R.id.btn_decrement)
        val durationDropdown = dialog.findViewById<View>(R.id.dd_duration)
        val setOnDropdown = dialog.findViewById<View>(R.id.dd_set_on)
        val submitButton = dialog.findViewById<Button>(R.id.btnSubmit)

        val durationAutoCompleteTextView =
            durationDropdown.findViewById<AutoCompleteTextView>(R.id.auto_complete_text)
        val setOnAutoCompleteTextView =
            setOnDropdown.findViewById<AutoCompleteTextView>(R.id.auto_complete_text)

        // Dropdown options
        val durationOptions = listOf(
            getString(R.string.minutes), getString(R.string.hours), getString(
                R.string.days
            )
        )
        val setOnOptions = listOf(
            getString(R.string.homescreen), getString(R.string.lockscreen), getString(R.string.both)
        )

        // Helper function to setup dropdown
        fun setupDropdown(
            view: AutoCompleteTextView, options: List<String>, defaultOption: String,
        ) {
            view.setAdapter(
                ArrayAdapter(
                    requireContext(), android.R.layout.simple_dropdown_item_1line, options
                )
            )
            view.setText(defaultOption, false)
        }

        // Initialize quantity
        var quantity = 15
        tvQuantity.text = quantity.toString()

        // Setup dropdowns
        setupDropdown(durationAutoCompleteTextView, durationOptions, durationOptions[0])
        setupDropdown(setOnAutoCompleteTextView, setOnOptions, setOnOptions[0])

        // Update quantity when duration changes
        durationAutoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val selectedDuration = s.toString()
                quantity = if (selectedDuration == getString(R.string.minutes)) 15 else 1
                tvQuantity.text = quantity.toString()
            }
        })

        // Increment/Decrement logic
        btnIncrement.setOnClickListener {
            quantity++
            tvQuantity.text = quantity.toString()
        }
        btnDecrement.setOnClickListener {
            val minQuantity =
                if (durationAutoCompleteTextView.text.toString() == getString(R.string.minutes)) 15 else 1
            if (quantity > minQuantity) {
                quantity--
                tvQuantity.text = quantity.toString()
            }
        }

        // Submit button click listener
        submitButton.setOnClickListener {
            val selectedDuration = durationAutoCompleteTextView.text.toString()
            val selectedSetOn = setOnAutoCompleteTextView.text.toString()
            val timeUnit = when (selectedDuration) {
                getString(R.string.minutes) -> TimeUnit.MINUTES
                getString(R.string.hours) -> TimeUnit.HOURS
                getString(R.string.days) -> TimeUnit.DAYS
                else -> throw IllegalArgumentException("Invalid duration")
            }
            val setType = when (selectedSetOn) {
                getString(R.string.homescreen) -> HOME_SCREEN
                getString(R.string.lockscreen) -> LOCK_SCREEN
                getString(R.string.both) -> BOTH_SCREENS
                else -> throw IllegalArgumentException("Invalid duration")
            }
            scheduleWork(quantity, timeUnit, setType)
            dialog.dismiss()
        }
        // Show dialog with adjusted size
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun scheduleWork(quantity: Int, duration: TimeUnit, wallpaperSetType: Int) {
        // Prepare the list of selected wallpaper IDs
        val selectedWallpaperIds = adapter.selectedItemList.map { adapter.listOfItems[it].id }
        // Reset previous selections and configurations
        viewModel.resetSelectedWallpapers()
        val prefs = SharedPrefs(requireContext())
        prefs.saveInt(0)
        prefs.saveWallpaperSetType(wallpaperSetType)
        WorkManager.getInstance(requireContext()).cancelAllWork()
        viewModel.markSelectedImages(selectedWallpaperIds)
        // Start the periodic worker request
        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(WallpaperWorker::class.java, quantity.toLong(), duration)
                .build()
        WorkManager.getInstance(requireContext()).enqueue(periodicWorkRequest)
        showToast(getString(R.string.wallpaper_change_scheduled))
    }

    private fun toggleSelection(position: Int) {
        if (adapter.selectedItemList.contains(position)) {
            adapter.selectedItemList.remove(position)
        } else {
            adapter.selectedItemList.add(position)
        }
        adapter.notifyItemChanged(position) // Ensure the specific item is updated
        updateDeleteButtonVisibility()
    }

    private fun updateDeleteButtonVisibility() {
        if (adapter.selectedItemList.isEmpty()) {
            binding.btnDelete.visibility = View.GONE
            isSelected = false
        } else {
            binding.btnDelete.visibility = View.VISIBLE
        }
    }

    override fun setUpObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favWallpapers.collectLatest { favWallpaperList ->
                    setUpWallpapers(favWallpaperList)
                    if (adapter.listOfItems.size == 0) setUpEmptyFavLayout()
                }
            }
        }
    }

    private fun setUpEmptyFavLayout() {
        binding.rvFavWallpapers.visibility = View.GONE
        binding.noFavLayout.visibility = View.VISIBLE
    }
}