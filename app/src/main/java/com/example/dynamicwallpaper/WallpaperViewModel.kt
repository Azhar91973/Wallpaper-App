package com.example.dynamicwallpaper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamicwallpaper.Database.FavouriteImageDataBase
import com.example.dynamicwallpaper.Models.WallpaperItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperViewModel @Inject constructor(private val wallpaperRepository: WallpaperRepository) :
    ViewModel() {

    private val _wallpapers = MutableLiveData<WallpaperItems>()
    val wallpapers: LiveData<WallpaperItems> = _wallpapers

    private val _searchWallpapers = MutableLiveData<WallpaperItems>()
    val searchWallpapers: LiveData<WallpaperItems> = _searchWallpapers

    private val _favWallpapers = MutableLiveData<List<FavouriteImageDataBase>>()
    val favWallpapers: LiveData<List<FavouriteImageDataBase>> = _favWallpapers

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getWallpapers(page: Int, query: String?, type: String) {
        viewModelScope.launch {
            val wallpaperResponse = wallpaperRepository.getWallpapers(page, query, type)

            if (wallpaperResponse.isSuccessful) {
                if(type == "search")
                    _searchWallpapers.value = wallpaperResponse.body()
                else
                _wallpapers.value = wallpaperResponse.body()
            }
            else _error.value = wallpaperResponse.message()
        }
    }

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
}