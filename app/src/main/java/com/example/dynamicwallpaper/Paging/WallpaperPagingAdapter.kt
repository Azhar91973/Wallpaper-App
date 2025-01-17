package com.example.dynamicwallpaper.Paging

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.R
import com.google.android.material.imageview.ShapeableImageView
import kotlin.random.Random

class WallpaperPagingAdapter(
    private val onImageClick: (Int) -> Unit, private val onFavClick: (Photo) -> Unit
) : PagingDataAdapter<Photo, WallpaperPagingAdapter.WallpaperViewHolder>(COMPARATOR) {

    private val rnd = Random

    class WallpaperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ShapeableImageView = itemView.findViewById(R.id.img_wallpaper)
        private val favImg: ConstraintLayout = itemView.findViewById(R.id.ic_fav)

        fun bind(
            item: Photo,
            onImageClick: (Int) -> Unit,
            onFavClick: (Photo) -> Unit,
            position: Int,
            rnd: Random
        ) {

            // Load image with Glide
            Glide.with(itemView.context).load(item.src.portrait)
                // TODO Add the images to drawable
//                .placeholder(R.drawable.placeholder) // Add appropriate placeholder
//                .error(R.drawable.error_image) // Add appropriate error image
                .into(imageView)

            // Apply random background color
            imageView.setBackgroundColor(
                Color.argb(
                    150, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)
                )
            )

            // Set click listeners
            imageView.setOnClickListener { onImageClick(position) }
            favImg.setOnClickListener { onFavClick(item) }
        }
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.bind(item, onImageClick, onFavClick, position, rnd)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.wallpaper_item, parent, false)
        return WallpaperViewHolder(view)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem == newItem
            }
        }
    }
}
