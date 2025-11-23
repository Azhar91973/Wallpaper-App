package com.example.dynamicwallpaper.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Common.CustomLoadStateAdapter
import com.example.dynamicwallpaper.Common.RecentSearchAdapter
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.Paging.WallpaperPagingAdapter
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.WallpaperViewModel
import com.example.dynamicwallpaper.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    private val viewModel: WallpaperViewModel by activityViewModels()
    private lateinit var pagingAdapter: WallpaperPagingAdapter
    private lateinit var recentSearchAdapter: ArrayAdapter<String>
    private var query: String? = null

    // initialising speechRecognizer
    private val speechRecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(
            requireContext()
        )
    }

    // taking record permission from the user
    private val allowPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            it?.let {
                if (it) {
                    showToast("Permission Granted")
                }
            }
        }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?,
    ): FragmentSearchBinding = FragmentSearchBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleNavArguments()
        setUpViews()
        setUpObservers()
        setUpClickListeners()
    }

    override fun setUpViews() = with(binding) {
        // Set up RecyclerView
        pagingAdapter = WallpaperPagingAdapter(::onItemClicked, ::onFavClick)

        rvSearchWallpapers.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            setHasFixedSize(true)
            adapter =
                pagingAdapter.withLoadStateFooter(footer = CustomLoadStateAdapter { pagingAdapter.retry() })
        }

        if (!(viewModel.hasSearched) && query == null) {
            viewModel.hasSearched = true
            setUpRecentSearch() // Show recent searches only if user j
        } else {
            rvSearchWallpapers.visibility = View.VISIBLE
            recentSearchLayout.visibility = View.GONE
            tvNoResult.visibility = View.GONE
        }
    }


    private fun handleNavArguments() = with(binding) {
        query = arguments?.getString("query")
        query?.let { query ->
            searchView.visibility = View.INVISIBLE
            tvCategoryName.apply {
                visibility = View.VISIBLE
                text = query
            }
        }

        arguments?.getBoolean("flag")?.let { isFromCategory ->
            if (isFromCategory) {
                arguments?.getString("query")?.let { viewModel.searchWallpaper(it) }
                arguments?.remove("flag")
            }
        }
    }

    private fun setUpRecentSearch() = with(binding) {
        val recentSearch = viewModel.getRecentSearchList()
        if (recentSearch.isEmpty()) return
        rvSearchWallpapers.visibility = View.GONE
        recentSearchLayout.visibility = View.VISIBLE
        recentSearchAdapter = RecentSearchAdapter(
            requireContext(), recentSearch.toMutableList(), ::removeRecentSearch, ::setSearchQuery
        )
        lvSearchedWallpapers.adapter = recentSearchAdapter
    }

    private fun setSearchQuery(query: String) {
        binding.searchView.setQuery(query, false)
    }

    private fun removeRecentSearch(query: String?) {
        if (query != null) {
            recentSearchAdapter.remove(query)
            viewModel.removeItemToRecentSearchList(query)
        } else {
            recentSearchAdapter.clear()
            viewModel.clearRecentSearchList()
        }
        recentSearchAdapter.notifyDataSetChanged()
        if (viewModel.getRecentSearchList().isEmpty()) binding.recentSearchLayout.visibility =
            View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setUpClickListeners() = with(binding) {
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val recentSearch = viewModel.getRecentSearchList()
                    if ((recentSearch.isEmpty() || recentSearch[0] != it) || pagingAdapter.itemCount == 0) {
                        // If the query is new, clear and add to recent search
                        if (recentSearch.isEmpty() || recentSearch[0] != it) {
                            viewModel.clearSearchedWallpaper()
                            viewModel.addItemToRecentSearchList(it)
                        }
                        viewModel.searchWallpaper(it)
                        pb.visibility = View.VISIBLE
                    }
                    searchView.clearFocus()
                    rvSearchWallpapers.visibility = View.VISIBLE
                    recentSearchLayout.visibility = View.GONE
                }
                return true
            }

            override fun onQueryTextChange(newText: String?) = true
        })

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                tvNoResult.visibility = View.GONE
                setUpRecentSearch()
            }
        }
        binding.btnListen.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    // when user release the listen btn
                    binding.searchView.queryHint = getString(R.string.find_wallpaper)
                    speechRecognizer.stopListening()
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_DOWN -> {
                    // when user press the listen btn
                    getPermissionOverO(requireContext()) {
                        startListen()
                    }
                    return@setOnTouchListener true
                }

                else -> {
                    return@setOnTouchListener true
                }
            }
        }
        lvSearchedWallpapers.setOnItemClickListener { _, _, position, _ ->
            val selectedQuery = recentSearchAdapter.getItem(position)
            selectedQuery?.let { searchView.setQuery(it, true) }

        }

        imgBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        tvClearAll.setOnClickListener {
            recentSearchAdapter.clear()
            removeRecentSearch(null)
            binding.recentSearchLayout.visibility = View.GONE
        }
    }

    private fun startListen() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onBeginningOfSpeech() {
                binding.searchView.queryHint = getString(R.string.listening)
            }

            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(p0: Int) {}
            override fun onResults(bundle: Bundle?) {
                bundle?.let {
                    val result = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    // set the best result
                    result?.get(0)?.let { it1 -> setSearchQuery(it1) }
                    binding.searchView.queryHint = getString(R.string.find_wallpaper)
                }
            }

            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
        speechRecognizer.startListening(intent)
    }


    private fun getPermissionOverO(context: Context, call: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    context, android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                call.invoke()
            } else {
                allowPermission.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }

    }

    override fun setUpObservers() {
        // Observe wallpapers data
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchedWallpapers.collectLatest { pagingData ->
                    pagingAdapter.submitData(pagingData)
                }
            }
        }
        // Observe load state
        pagingAdapter.addLoadStateListener { loadState ->
            with(binding) {
                when (loadState.refresh) {
                    is LoadState.Loading -> {
                        pb.visibility = View.VISIBLE
                    }

                    is LoadState.Error -> {
                        pb.visibility = View.GONE
                        tvNoResult.visibility = View.VISIBLE
                    }

                    else -> {
                        searchView.clearFocus()
                        pb.visibility = View.GONE
                        tvNoResult.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun onFavClick(item: Photo) {
        viewModel.toggleFavorite(FavouriteImageDataBase(item.src.portrait)) { success ->
            if (success) showToast(getString(R.string.image_added_to_favourite))
        }
    }

    private fun onItemClicked(position: Int) {
        viewModel.selectedPosition = position
        findNavController().navigate(
            R.id.action_searchFragment_to_viewWallpaperFragment, bundleOf("source" to "search")
        )
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setBottomNavigationVisibility(false)
    }

    override fun onDestroyView() {
        binding.rvSearchWallpapers.adapter = null
        binding.lvSearchedWallpapers.adapter = null
        if (findNavController().currentDestination?.id !in listOf(
                R.id.searchFragment, R.id.viewWallpaperFragment
            )
        ) {
            viewModel.hasSearched = false
            viewModel.clearSearchedWallpaper()
            (requireActivity() as MainActivity).setBottomNavigationVisibility(true)
        }
        super.onDestroyView()
    }
}