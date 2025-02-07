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
import androidx.activity.OnBackPressedCallback
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
import javax.inject.Inject

@AndroidEntryPoint
class FavouriteFragment : BaseFragment<FragmentFavouriteBinding>() {
    private val viewModel: WallpaperViewModel by activityViewModels()
    // Our adapter is typed with both the data item and its binding.

    @Inject
    lateinit var sharedPrefs: SharedPrefs
    private lateinit var adapter: BaseAdapter<FavouriteImageDataBase, WallpaperItemBinding>
    private lateinit var drawerLayout: DrawerLayout

    // This flag indicates if selection mode is active.
    private var isSelected: Boolean = false

    // Hold a reference to the current wallpaper list (used when deleting items)
    private var wallpapersList: List<FavouriteImageDataBase> = emptyList()

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?,
    ): FragmentFavouriteBinding {
        return FragmentFavouriteBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPress()
        setUpDrawerLayout()
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    private fun handleBackPress() {
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (adapter.getSelectedCount() == 0) {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } else {
                    adapter.clearSelections()
                    updateDeleteButtonVisibility()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
    }

    override fun setUpViews() {
        // Additional view setup if needed.
    }

    private fun setUpWallpapers(wallpapers: List<FavouriteImageDataBase>) {
        wallpapersList = wallpapers
        adapter = object : BaseAdapter<FavouriteImageDataBase, WallpaperItemBinding>() {
            override fun createBinding(parent: ViewGroup): WallpaperItemBinding {
                return WallpaperItemBinding.inflate(layoutInflater, parent, false)
            }

            override fun getItemId(item: FavouriteImageDataBase): String {
                // Convert the id to string if necessary.
                return item.id.toString()
            }

            override fun bindView(
                binding: WallpaperItemBinding,
                item: FavouriteImageDataBase,
                isSelected: Boolean,
            ) {
                Glide.with(binding.root.context).load(item.imageUrl).into(binding.imgWallpaper)
                binding.icFav.visibility = View.GONE
                binding.selectedFav.visibility = if (isSelected) View.VISIBLE else View.GONE
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

        // Set up click and long-click listeners using the adapter’s callbacks.
        adapter.onItemClick = { item ->
            // If the item is already selected or selection mode is active, toggle its selection.
            if (adapter.isSelected(item) || isSelected) {
                adapter.toggleSelection(adapter.getItemId(item))
                updateDeleteButtonVisibility()
            } else {
                // Otherwise, treat the click as a normal item click.
                val index = wallpapersList.indexOf(item)
                viewModel.selectedPosition = index
                Log.d("FavPosition", "setUpWallpapers: Position = $index")
                findNavController().navigate(
                    R.id.action_favouriteFragment_to_viewWallpaperFragment,
                    bundleOf("source" to "Favourite")
                )
            }
        }

        adapter.onItemLongClick = { item ->
            if (!adapter.isSelected(item)) {
                adapter.toggleSelection(adapter.getItemId(item))
                isSelected = true
                binding.btnDelete.visibility = View.VISIBLE
            }
            true
        }

        adapter.submitList(wallpapers)
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
            showDeleteConfirmationDialog()
        }
        binding.option.setOnClickListener {
            if (adapter.getSelectedCount() > 1) openDialog()
            else showToast(getString(R.string.play_wallpaper_info))
        }
        binding.icMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.btnExplore.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_delete_confirmation)
            setCancelable(true)
        }
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        btnConfirm.setOnClickListener {
            deleteSelectedItems()
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun deleteSelectedItems() {
        val selectedIds = adapter.getSelectedIds()
        // Remove selected items from the local list.
        val remainingItems = wallpapersList.filter { adapter.getItemId(it) !in selectedIds }
        wallpapersList.filter { adapter.getItemId(it) in selectedIds }
            .forEach { viewModel.deleteFavImage(it) }
        wallpapersList = remainingItems
        adapter.submitList(remainingItems)
        adapter.clearSelections()
        updateDeleteButtonVisibility()
        if (remainingItems.isEmpty()) setUpEmptyFavLayout()
    }

    private fun openDialog() {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.custom_dialog)
        }
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

        val durationOptions = listOf(
            getString(R.string.minutes), getString(R.string.hours), getString(R.string.days)
        )
        val setOnOptions = listOf(
            getString(R.string.homescreen), getString(R.string.lockscreen), getString(R.string.both)
        )

        fun setupDropdown(
            view: AutoCompleteTextView,
            options: List<String>,
            defaultOption: String,
        ) {
            view.setAdapter(
                ArrayAdapter(
                    requireContext(), android.R.layout.simple_dropdown_item_1line, options
                )
            )
            view.setText(defaultOption, false)
        }

        var quantity = 15
        tvQuantity.text = quantity.toString()
        setupDropdown(durationAutoCompleteTextView, durationOptions, durationOptions[0])
        setupDropdown(setOnAutoCompleteTextView, setOnOptions, setOnOptions[0])

        durationAutoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val selectedDuration = s.toString()
                quantity = if (selectedDuration == getString(R.string.minutes)) 15 else 1
                tvQuantity.text = quantity.toString()
            }
        })

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
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun scheduleWork(quantity: Int, duration: TimeUnit, wallpaperSetType: Int) {
        val selectedWallpaperIds = adapter.getSelectedIds().map { it.toInt() }
        Log.d("SelectedIds", "scheduleWork: $selectedWallpaperIds")
        viewModel.resetSelectedWallpapers()
        val prefs = sharedPrefs
        prefs.saveInt(0)
        prefs.saveWallpaperSetType(wallpaperSetType)
        WorkManager.getInstance(requireContext()).cancelAllWork()
        viewModel.markSelectedImages(selectedWallpaperIds)
        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(WallpaperWorker::class.java, quantity.toLong(), duration)
                .build()
        WorkManager.getInstance(requireContext()).enqueue(periodicWorkRequest)
        showToast(getString(R.string.wallpaper_change_scheduled))
    }

    private fun updateDeleteButtonVisibility() {
        if (adapter.getSelectedCount() == 0) {
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
                    if (favWallpaperList.isEmpty()) setUpEmptyFavLayout()
                }
            }
        }
    }

    private fun setUpEmptyFavLayout() {
        binding.rvFavWallpapers.visibility = View.GONE
        binding.noFavLayout.visibility = View.VISIBLE
    }
}
