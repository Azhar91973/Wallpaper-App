package com.example.dynamicwallpaper

import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.dynamicwallpaper.Common.SharedPrefs
import com.example.dynamicwallpaper.Common.SharedPrefs.Companion.RECENT_SEARCH_LIST
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.Models.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperViewModel @Inject constructor(private val wallpaperRepository: WallpaperRepository) :
    ViewModel() {

    // State for recycler view scroll position
    var recyclerViewState: Parcelable? = null

    // State for selected position in RecyclerView
    var selectedPosition: Int? = null

    // PagingData for wallpapers
    val wallpapers = wallpaperRepository.getWallpapers().cachedIn(viewModelScope)

    // StateFlow for searched wallpapers to ensure lifecycle awareness and better performance
    private val _searchedWallpapers = MutableStateFlow<PagingData<Photo>?>(null)
    val searchedWallpapers: StateFlow<PagingData<Photo>?> = _searchedWallpapers

    // LiveData for favorite wallpapers
    private val _favWallpapers = MutableLiveData<List<FavouriteImageDataBase>>()
    val favWallpapers: LiveData<List<FavouriteImageDataBase>> = _favWallpapers

    // Insert a favorite image
    fun insertFavImage(imgUrl: FavouriteImageDataBase) = viewModelScope.launch {
        wallpaperRepository.insertFavImage(imgUrl)
    }

    fun toggleFavorite(item: FavouriteImageDataBase, onResult: (Boolean) -> Unit) =
        viewModelScope.launch {
            try {
                // Check if the image is already in favorites
                val isFavorite = wallpaperRepository.getImageByUrl(item.imageUrl)
                if (!isFavorite) {
                    // If not a favorite, add it
                    wallpaperRepository.insertFavImage(item)
                    onResult(true) // Indicate that it was added
                }
            } catch (e: Exception) {
                // Handle any errors that might occur
                e.printStackTrace()
                onResult(false) // Indicate failure
            }
        }


    fun getRecentSearchList(context: Context): List<String> =
        SharedPrefs(context).getListFromPreferences(RECENT_SEARCH_LIST)


    fun addItemToRecentSearchList(context: Context, item: String) =
        SharedPrefs(context).addItemToPreferences(RECENT_SEARCH_LIST, item)

    fun removeItemToRecentSearchList(context: Context, item: String) =
        SharedPrefs(context).removeItemFromPreferences(RECENT_SEARCH_LIST, item)


    fun clearRecentSearchList(context: Context) =
        SharedPrefs(context).clearListFromPreferences(RECENT_SEARCH_LIST)

    // Delete a favorite image
    fun deleteFavImage(imgUrl: FavouriteImageDataBase) = viewModelScope.launch {
        wallpaperRepository.deleteFavImage(imgUrl)
    }

    // Mark selected images
    fun markSelectedImages(ids: List<Int>) = viewModelScope.launch {
        wallpaperRepository.markSelectedImages(ids)
    }


    // Reset selected wallpapers
    fun resetSelectedWallpapers() = viewModelScope.launch {
        wallpaperRepository.resetSelectedWallpapers()
    }


    // Get selected images (for debugging/logging purposes)
    fun getSelectedImages() = viewModelScope.launch {
        val selectedImages = wallpaperRepository.getSelectedImages()
        selectedImages.let {
            Log.d("SelectedImages", "getSelectedImages: $it")
        }
    }


    // Check if an image is already in favorites
    fun getImageByUrl(imageUrl: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val isPresent = wallpaperRepository.getImageByUrl(imageUrl)
        onResult(isPresent)
    }


    // Fetch all favorite images
    fun getAllFavImages() = viewModelScope.launch {
        _favWallpapers.value = wallpaperRepository.getAllFavImages()
    }

    // Search wallpapers by query
    fun searchWallpaper(query: String) = viewModelScope.launch {
        wallpaperRepository.getWallpapers("search", query).cachedIn(viewModelScope)
            .collectLatest { pagingData ->
                _searchedWallpapers.value = pagingData
            }
    }

    // Clear searched wallpapers
    fun clearSearchedWallpaper() {
        _searchedWallpapers.value = null
    }
}