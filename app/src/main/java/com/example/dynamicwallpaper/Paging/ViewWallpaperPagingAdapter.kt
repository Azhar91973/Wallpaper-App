package com.example.dynamicwallpaper.Paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.databinding.SetWallpaperItemBinding

class ViewWallpaperPagingAdapter(
    private val setWallpaper: (String) -> Unit,
    private val favImage: (Photo) -> Unit,
    private val downloadImage: (String) -> Unit,
    private val backBtnClicked: () -> Unit
) : PagingDataAdapter<Photo, ViewWallpaperPagingAdapter.ViewWallpaperViewHolder>(COMPARATOR) {

    inner class ViewWallpaperViewHolder(private val binding: SetWallpaperItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Photo) {
            Glide.with(binding.root.context).load(item.src.portrait).into(binding.viewWallpaper)
            binding.imgAddFavourite.setOnClickListener { favImage(item) }
            binding.imgShare.setOnClickListener { downloadImage(item.src.portrait) }
            binding.imgSetWallpaper.setOnClickListener { setWallpaper(item.src.portrait) }
            binding.imgBackBtn.setOnClickListener { backBtnClicked() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewWallpaperViewHolder {
        val binding = SetWallpaperItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewWallpaperViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewWallpaperViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo) = oldItem == newItem
        }
    }
}
