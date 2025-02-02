package com.example.dynamicwallpaper.Paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.Network.WallpaperApi


class WallpaperPagingSource(
    private val wallpaperApi: WallpaperApi
) : PagingSource<Int, Photo>() {
    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {

        if (state.anchorPosition != null) {
            val anchorPosition = state.closestPageToPosition(state.anchorPosition!!)
            if (anchorPosition?.prevKey != null) {
                return anchorPosition.prevKey!!.plus(1)
            } else if (anchorPosition?.nextKey != null) {
                return anchorPosition.nextKey!!.minus(1)
            }
        }
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        try {
            val currentPage = params.key ?: 1
            val response = wallpaperApi.getWallpaper(currentPage)
            Log.d("WallpaperResponse", "load: ${response.photos}")
            return LoadResult.Page(
                data = response.photos.shuffled(),
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (currentPage == response.total_results) null else currentPage + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}