package com.example.dynamicwallpaper

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.Models.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperViewModel @Inject constructor(private val wallpaperRepository: WallpaperRepository) :
    ViewModel() {
    var recyclerViewState: Parcelable? = null
    var selectedPosition: Int? = null
    val wallpapers = wallpaperRepository.getWallpapers().cachedIn(viewModelScope)

    private val _searchedWallpapers = MutableLiveData<PagingData<Photo>>()
    val searchedWallpapers: LiveData<PagingData<Photo>> = _searchedWallpapers

    private val _favWallpapers = MutableLiveData<List<FavouriteImageDataBase>>()
    val favWallpapers: LiveData<List<FavouriteImageDataBase>> = _favWallpapers

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun insertFavImage(imgUrl: FavouriteImageDataBase) {
        viewModelScope.launch {
            wallpaperRepository.insertFavImage(imgUrl)
        }
    }

    fun getAllFavImages() {
        viewModelScope.launch {
            _favWallpapers.value = wallpaperRepository.getAllFavImages()
        }
    }

    fun searchWallpaper(query: String) {
        viewModelScope.launch {
            wallpaperRepository.getWallpapers("search", query).cachedIn(viewModelScope)
                .observeForever {
                    _searchedWallpapers.postValue(it)
                }
        }
    }
}