package com.example.dynamicwallpaper.Paging

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dynamicwallpaper.Models.Photo
import com.example.dynamicwallpaper.R
import com.google.android.material.imageview.ShapeableImageView

class ViewWallpaperPagingAdapter(
    private val setWallpaper: (String) -> Unit,
    private val favImage: (String) -> Unit,
    private val shareImage: (String) -> Unit,
    private val backBntClicked: () -> Unit
) : PagingDataAdapter<Photo, ViewWallpaperPagingAdapter.ViewWallpaperViewHolder>(COMPARATOR) {

    class ViewWallpaperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ShapeableImageView = itemView.findViewById(R.id.view_wallpaper)
        val favWallpaper: ImageView = itemView.findViewById(R.id.img_add_favourite)
        val shareWallpaper: ImageView = itemView.findViewById(R.id.img_share)
        val setWallpaper: ImageView = itemView.findViewById(R.id.img_set_wallpaper)
        val backBtn: ImageView = itemView.findViewById(R.id.img_back_btn)
        fun bind(item: Photo) {
            Log.d("Response Size", "load: ${item.src.original}")
            Glide.with(itemView.context).load(item.src.portrait).into(imageView)
        }
    }

    override fun onBindViewHolder(holder: ViewWallpaperViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
        holder.favWallpaper.setOnClickListener {
            favImage(item.src.portrait)
        }
        holder.shareWallpaper.setOnClickListener {
            shareImage(item.src.portrait)
        }
        holder.setWallpaper.setOnClickListener {
            setWallpaper(item.src.portrait)
        }
        holder.backBtn.setOnClickListener {
            backBntClicked()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewWallpaperViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.set_wallpaper_item, parent, false)
        return ViewWallpaperViewHolder(view)
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